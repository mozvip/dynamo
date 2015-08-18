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
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

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
@RequestScoped
public class Games extends DynamoManagedBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<GamePlatform, Path> folder;
	private String newGameName;
	private GamePlatform newGamePlatform;
	private long searchId;

	@ManagedProperty("#{param.platform}")
	private GamePlatform platform;

	@ManagedProperty("#{param.status1}")
	private DownloadableStatus status1;

	@ManagedProperty("#{param.status2}")
	private DownloadableStatus status2;

	@ManagedProperty("#{param.page}")
	private int page = 1;

	private DownloadablePager<VideoGame> games;

	public DownloadablePager<VideoGame> getGames() {
		if (games == null) {
			List<VideoGame> gamesToDisplay = GamesManager.getInstance().getGames(platform, status1, status2 );
			games = new DownloadablePager<>( gamesToDisplay );
			games.goToPage(page - 1);
		}
		return games;
	}

	public GamePlatform getPlatform() {
		return platform;
	}

	public void setPlatform(GamePlatform platform) {
		this.platform = platform;
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

	public Games() {
		folder = new HashMap<GamePlatform, Path>();
		List<GamePlatform> platforms = GamesManager.getInstance().getPlatforms();
		for (GamePlatform platform : platforms) {
			folder.put(platform, GamesManager.getInstance().getFolder(platform));
		}
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
		for (Iterator<VideoGame> iterator = getGames().objects.iterator(); iterator.hasNext();) {
			VideoGame videoGame = iterator.next();
			if (videoGame.getId() == id) {
				queue(new InvokeMethodTask(GamesManager.getInstance(), "changeImage", String.format("Finding new cover art for %s", videoGame.toString()),
						videoGame), true);
				iterator.remove();
			}
		}
	}

	public void delete() {
		queue(new DeleteDownloadableTask(games.remove(getIntegerParameter("id"))));
	}

	public void save() {
		try {
			for (GamePlatform platform : getPlatforms()) {
				Path folderForPlatform = folder.get(platform);
				GamesManager.getInstance().setPlatformFolder(platform, folderForPlatform);
			}
			EventManager.getInstance().reportInfo("Game settings saved successfully");
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
	}

	public void redownload() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		long id = getIntegerParameter("id");
		DownloadableManager.getInstance().redownload(id);
	}

	public void downloadGame(long theGamesDbId) throws MalformedURLException {
		GamesManager.getInstance().want(theGamesDbId);
	}

	private List<TheGamesDBGame> suggestedGames = null;

	public void search() {
		GetGamesListResponse response = TheGamesDB.getInstance().getGamesList(newGameName, newGamePlatform.getLabel(), null);
		suggestedGames = response.getGames();
	}

	public void select(TheGamesDBGame game) throws MalformedURLException {
		GamesManager.getInstance().associate(searchId, game);
		games = null;
	}

	public List<TheGamesDBGame> getSuggestedGames() {
		return suggestedGames;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public DownloadableStatus getStatus1() {
		return status1;
	}

	public void setStatus1(DownloadableStatus status1) {
		this.status1 = status1;
	}

	public DownloadableStatus getStatus2() {
		return status2;
	}

	public void setStatus2(DownloadableStatus status2) {
		this.status2 = status2;
	}
	
	

}
