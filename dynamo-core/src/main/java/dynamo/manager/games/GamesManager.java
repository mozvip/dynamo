package dynamo.manager.games;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import core.WebResource;
import dynamo.backlog.BackLogProcessor;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ConfigValueManager;
import dynamo.core.manager.DAOManager;
import dynamo.finders.core.GameFinder;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.DownloadableStatus;
import dynamo.model.games.GamePlatform;
import dynamo.model.games.VideoGame;
import dynamo.model.games.VideoGameDAO;
import dynamo.webapps.googleimages.GoogleImages;
import dynamo.webapps.thegamesdb.net.GetArtResponse;
import dynamo.webapps.thegamesdb.net.TheGamesDB;
import dynamo.webapps.thegamesdb.net.TheGamesDBGame;
import dynamo.webapps.thegamesdb.net.images.TheGamesDBBoxArt;

public class GamesManager implements Reconfigurable {
	
	@Configurable(category="Games", name="Enable Games", bold=true)
	private boolean enabled;
	
	@Configurable(category="Games", name="Platforms", disabled="#{!GamesManager.enabled}", contentsClass=GamePlatform.class)
	private List<GamePlatform> platforms;	
	
	@Configurable(category="Games", name="Game Providers", required="#{GamesManager.enabled}", disabled="#{!GamesManager.enabled}", contentsClass=GameFinder.class, ordered=true )
	private List<GameFinder> providers;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	public List<GamePlatform> getPlatforms() {
		return platforms;
	}

	public void setPlatforms(List<GamePlatform> platforms) {
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
		ConfigValueManager.getInstance().setConfigString( getFolderConfigKey(platform), folder.toAbsolutePath().toString() );
		ConfigValueManager.getInstance().persistConfiguration();
	}

	public Path getFolder(GamePlatform platform) {
		String pathValue = ConfigValueManager.getInstance().getConfigString( getFolderConfigKey(platform) );
		return pathValue != null ? Paths.get( pathValue ) : null;
	}

	public List<VideoGame> getGames( GamePlatform platform, DownloadableStatus status1, DownloadableStatus status2 ) {
		
		if (platform == null) {
			if (status2 == null) {
				return videoGameDAO.findAll( status1 );
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
	
	public void changeImage( VideoGame game ) throws MalformedURLException {
		String image = getLocalImage(game.getTheGamesDbId(), game.getName(), game.getPlatform() );
		DownloadableManager.getInstance().updateCoverImage( game.getId(), image);		
	}
	
	private String getLocalImage( long theGamesDbId, String title, GamePlatform platform ) {
		String imageId = String.format("%s-%s", title, platform.name());

		GetArtResponse response = TheGamesDB.getInstance().getArt(theGamesDbId);
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
			WebResource resource = findImage( title, platform );
			if (resource != null) {
				imageURL = resource.getUrl();
				imageReferer = resource.getReferer();
			}
		}

		return imageURL != null ? LocalImageCache.getInstance().download("games", imageId, imageURL, imageReferer) : null;
	}
	
	public VideoGame findByTheGamesDbId( long theGamesDbId ) {
		return videoGameDAO.findByTheGamesDbId(theGamesDbId);
	}

	public VideoGame createGame( String title, String platform, long theGamesDbId, Path folder, DownloadableStatus status )  {

		GamePlatform newGamePlatform = GamePlatform.match( platform );
		String image = getLocalImage(theGamesDbId, title, newGamePlatform );
		long videoGameId = DownloadableManager.getInstance().createDownloadable(VideoGame.class, title, folder, image, status );
		VideoGame game = new VideoGame(videoGameId, status, folder, image, title, newGamePlatform, theGamesDbId );
		videoGameDAO.save( videoGameId, game.getPlatform(), game.getTheGamesDbId() );
		
		return game;
	}

	@Override
	public void reconfigure() {
		if (enabled) {
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

	public void associate(long videoGameId, TheGamesDBGame game ) throws MalformedURLException {

		GamePlatform newGamePlatform = GamePlatform.match( game.getPlatform() );
		String image = getLocalImage( game.getId(), game.getGameTitle(), newGamePlatform );
		DownloadableManager.getInstance().updateCoverImage( videoGameId, image );
		
		videoGameDAO.save( videoGameId, newGamePlatform, game.getId() );
	}

	public void want(long theGamesDbId) {
		VideoGame game = GamesManager.getInstance().findByTheGamesDbId( theGamesDbId );
		if (game == null) {
			TheGamesDBGame theGamesDbGame = TheGamesDB.getInstance().getGame( theGamesDbId );
			if (theGamesDbGame != null) {
				game = GamesManager.getInstance().createGame( theGamesDbGame.getGameTitle(), theGamesDbGame.getPlatform(), theGamesDbId, null, DownloadableStatus.WANTED );
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

}
