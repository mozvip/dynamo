package dynamo.ui;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.core.EventManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.tasks.InvokeMethodTask;
import dynamo.manager.DownloadableManager;
import dynamo.manager.games.GamesManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.games.GamePlatform;
import dynamo.model.games.VideoGame;
import dynamo.webapps.thegamesdb.net.GetGamesListResponse;
import dynamo.webapps.thegamesdb.net.TheGamesDB;
import dynamo.webapps.thegamesdb.net.TheGamesDBGame;

@ManagedBean
@SessionScoped
public class Games extends DynamoManagedBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<GamePlatform, Path> folder;
	private String newGameName;
	private GamePlatform newGamePlatform;
	private long searchId;
	
	private GamePlatform filterPlatform;
	private DownloadablePager<VideoGame> collection;
	private DownloadablePager<VideoGame> wanted;
	private DownloadablePager<VideoGame> suggested;
	private DownloadablePager<VideoGame> displayed;
	
	public DownloadablePager<VideoGame> getCollection() {
		if (collection == null) {
			collection = new DownloadablePager<>(GamesManager.getInstance().getGames( filterPlatform, DownloadableStatus.DOWNLOADED ));
			displayed = collection;
		}
		return collection;
	}
	
	public DownloadablePager<VideoGame> getWanted() {
		if (wanted == null) {
			List<VideoGame> wantedGames = GamesManager.getInstance().getGames( filterPlatform, DownloadableStatus.WANTED );
			wantedGames.addAll( GamesManager.getInstance().getGames( filterPlatform, DownloadableStatus.SNATCHED ) );
			wanted = new DownloadablePager<>(wantedGames);
			displayed = wanted;
		}
		return wanted;
	}
		
	public DownloadablePager<VideoGame> getSuggested() {
		if (suggested == null) {
			suggested = new DownloadablePager<>(GamesManager.getInstance().getGames( filterPlatform, DownloadableStatus.SUGGESTED ));
			displayed = suggested;
		}
		return suggested;
	}
	
	public void filter() {
		collection = null;
		wanted = null;
		suggested = null;
		displayed = null;
	}
	
	public GamePlatform getFilterPlatform() {
		return filterPlatform;
	}
	
	public void setFilterPlatform(GamePlatform filterPlatform) {
		this.filterPlatform = filterPlatform;
	}
	
	public long getSearchId() {
		return searchId;
	}
	
	public void setSearchId(long searchId) {
		this.searchId = searchId;
	}

	public String getNewGameName() {
		return newGameName;
	}

	public void setNewGameName(String newGameName) {
		this.newGameName = newGameName;
	}
	
	public GamePlatform getNewGamePlatform() {
		return newGamePlatform;
	}
	
	public void setNewGamePlatform(GamePlatform newGamePlatform) {
		this.newGamePlatform = newGamePlatform;
	}
	
	public List<GamePlatform> getNewGamePlatforms() {
		return GamesManager.getInstance().getPlatforms();
	}

	public Games() {
		folder = new HashMap<GamePlatform, Path>();
		List<GamePlatform> platforms = GamesManager.getInstance().getPlatforms();
		for (GamePlatform platform : platforms) {
			folder.put( platform, GamesManager.getInstance().getFolder( platform) );
		}
	}
	
	public List<VideoGame> getWantedGames() {
		return GamesManager.getInstance().getWantedAndSnatched();
	}

	public Map<GamePlatform, Path> getFolder() {
		return folder;
	}
	
	public void setFolder(Map<GamePlatform, Path> folder) {
		this.folder = folder;
	}
	
	public List<GamePlatform> getPlatforms() {
		return GamesManager.getInstance().getPlatforms();
	}
	
	public void changeImage() throws NoSuchMethodException, SecurityException {
		long id = getIntegerParameter("id");
		for (Iterator<VideoGame> iterator = getCollection().objects.iterator(); iterator.hasNext();) {
			VideoGame videoGame = iterator.next();
			if (videoGame.getId() == id) {
				queue( new InvokeMethodTask( GamesManager.getInstance(), "changeImage", String.format("Finding new cover art for %s", videoGame.toString()), videoGame), true);
				iterator.remove();
			}
		}
	}
	
	public void delete() {
		queue( new DeleteDownloadableTask( displayed.remove( getIntegerParameter("id") ) ) );
	}

	public void save() {
		try {
			for (GamePlatform platform : getPlatforms()) {
				Path folderForPlatform = folder.get( platform );
				GamesManager.getInstance().setPlatformFolder( platform, folderForPlatform );
			}
			EventManager.getInstance().reportInfo("Game settings saved successfully");
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}
	
	public void redownload() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		long id = getIntegerParameter("id");
		DownloadableManager.getInstance().redownload(id);
	}
	
	
	public void downloadGame(int id, String title, String platform) throws MalformedURLException {
		VideoGame game = GamesManager.getInstance().createGame( title, platform, id, null, DownloadableStatus.WANTED );
		DownloadableManager.getInstance().want( game );
	}

	private List<TheGamesDBGame> suggestedGames = null;
	
	public void search() {
		GetGamesListResponse response = TheGamesDB.getInstance().getGamesList(newGameName, newGamePlatform.getLabel(), null);
		suggestedGames = response.getGames();
	}
	
	public void select(TheGamesDBGame game) throws MalformedURLException {
		GamesManager.getInstance().associate( searchId, game );
		collection = null;
	}
	
	public List<TheGamesDBGame> getSuggestedGames() {
		return suggestedGames;
	}

}
