package com.github.dynamo.manager.games;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.manager.ConfigAnnotationManager;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.games.GameFinder;
import com.github.dynamo.games.model.GamePlatform;
import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.games.model.VideoGameDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.webapps.googleimages.GoogleImages;
import com.github.dynamo.webapps.thegamesdb.net.GetArtResponse;
import com.github.dynamo.webapps.thegamesdb.net.TheGamesDB;
import com.github.dynamo.webapps.thegamesdb.net.TheGamesDBGame;
import com.github.dynamo.webapps.thegamesdb.net.images.TheGamesDBBoxArt;
import com.github.mozvip.hclient.core.WebResource;

public class GamesManager implements Reconfigurable {

	@Configurable(contentsClass=GamePlatform.class)
	private Set<GamePlatform> platforms;	
	
	@Configurable(contentsClass=GameFinder.class, ordered=true )
	private List<GameFinder> providers;

	public boolean isEnabled() {
		return platforms != null && platforms.size() > 0;
	}

	public Set<GamePlatform> getPlatforms() {
		return platforms;
	}

	public void setPlatforms(Set<GamePlatform> platforms) {
		this.platforms = platforms;
	}
	
	private VideoGameDAO videoGameDAO = DAOManager.getInstance().getDAO( VideoGameDAO.class );

	private GamesManager() {
	}

	static class SingletonHolder {
		static GamesManager instance = new GamesManager();
	}

	public static GamesManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public List<GameFinder> getProviders() {
		return providers;
	}
	
	public void setProviders(List<GameFinder> providers) {
		this.providers = providers;
	}
	
	protected static String getFolderConfigKey( GamePlatform platform ) {
		return String.format( "%s.folder", platform.name() );
	}
	
	public void setPlatformFolder( GamePlatform platform, Path folder ) throws JsonGenerationException, JsonMappingException, IOException {
		ConfigAnnotationManager.getInstance().setConfigString( getFolderConfigKey(platform), folder.toAbsolutePath().toString() );
		ConfigAnnotationManager.getInstance().persistConfiguration();
	}

	public Path getFolder(GamePlatform platform) {
		String pathValue = ConfigAnnotationManager.getInstance().getConfigString( getFolderConfigKey(platform) );
		return pathValue != null ? Paths.get( pathValue ) : null;
	}

	public List<VideoGame> getGames( GamePlatform platform, DownloadableStatus status1, DownloadableStatus status2 ) {
		
		if (platform == null) {
			if (status2 == null) {
				return videoGameDAO.findByStatus( status1 );
			} else {
				return videoGameDAO.findAll( status1, status2 );
			}
		} else {
			if (status2 == null) {
				return videoGameDAO.findAll( status1, platform );
			} else {
				return videoGameDAO.findAll( status1, status2, platform );
			}
		}
	}

	public WebResource findImage( String gameName, GamePlatform platform ) {
		return GoogleImages.findImage( String.format("%s %s front cover", gameName, platform.name() ), platform.getCoverImageRatio());
	}
	
	public void changeImage( VideoGame game ) throws IOException {
		GetArtResponse response = TheGamesDB.getInstance().getArt( game.getTheGamesDbId() );
		List<TheGamesDBBoxArt> boxarts = response.getImages().getBoxarts();
		
		String imageURL = null;
		String imageReferer = null;
		
		if (boxarts != null) {
			for (TheGamesDBBoxArt theGamesDBBoxArt : boxarts) {
				if (StringUtils.equals(theGamesDBBoxArt.getSide(), "front")) {
					imageURL = response.getBaseImgUrl() + theGamesDBBoxArt.getPath(); 
					break;
				}
			}
		}
		
		if (imageURL == null) {
			WebResource resource = findImage( game.getName(), game.getPlatform() );
			if (resource != null) {
				imageURL = resource.getUrl();
				imageReferer = resource.getReferer();
			}
		}
		
		DownloadableManager.downloadImage( game, imageURL, imageReferer );
	}
	
	
	public VideoGame findByTheGamesDbId( long theGamesDbId ) {
		return videoGameDAO.findByTheGamesDbId(theGamesDbId);
	}

	public VideoGame createGame( String title, String platform, long theGamesDbId, DownloadableStatus status ) throws IOException  {

		long videoGameId = DownloadableManager.getInstance().createDownloadable(VideoGame.class, title, -1, status );

		GamePlatform newGamePlatform = GamePlatform.match( platform );
		VideoGame game = new VideoGame(videoGameId, status, title, newGamePlatform, theGamesDbId );
		videoGameDAO.save( videoGameId, game.getPlatform(), game.getTheGamesDbId() );
		changeImage(game);
		
		return game;
	}

	@Override
	public void reconfigure() {
		if (isEnabled()) {
			if (platforms != null) {
				for (GamePlatform platform : platforms) {
					Path path = getFolder(platform);
					if (path != null) {
						BackLogProcessor.getInstance().schedule( new ScanGamesFolderTask(platform, path));
					}
				}
			}
		} else {
			BackLogProcessor.getInstance().unschedule( ScanGamesFolderTask.class );
		}
	}

	public void want(long theGamesDbId) throws IOException {
		VideoGame game = GamesManager.getInstance().findByTheGamesDbId( theGamesDbId );
		if (game == null) {
			TheGamesDBGame theGamesDbGame = TheGamesDB.getInstance().getGame( theGamesDbId );
			if (theGamesDbGame != null) {
				game = GamesManager.getInstance().createGame( theGamesDbGame.getGameTitle(), theGamesDbGame.getPlatform(), theGamesDbId, DownloadableStatus.WANTED );
				if (theGamesDbGame.getAlternateTitles() != null && theGamesDbGame.getAlternateTitles().size() > 0) {
					DownloadableManager.getInstance().setAkas(game.getId(), theGamesDbGame.getAlternateTitles());
				}
				DownloadableManager.getInstance().scheduleFind( game );
			}
		} else {
			if (game.getStatus() != DownloadableStatus.DOWNLOADED) {
				DownloadableManager.getInstance().want( game );
			}
		}

	}

	public List<VideoGame> getGames(GamePlatform platform, DownloadableStatus status) {
		return getGames(platform, status, null);
	}

	public void associate(long videoGameId, TheGamesDBGame game) {
		
		// FIXME
		
		
	}

}
