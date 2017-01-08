package com.github.dynamo.manager.games;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.dynamo.backlog.tasks.core.ScanFolderExecutor;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.games.model.GamePlatform;
import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.webapps.thegamesdb.net.GetGamesListResponse;
import com.github.dynamo.webapps.thegamesdb.net.TheGamesDB;
import com.github.dynamo.webapps.thegamesdb.net.TheGamesDBGame;
import com.github.mozvip.hclient.core.RegExp;

public class ScanGamesFolderExecutor extends ScanFolderExecutor<ScanGamesFolderTask> {

	private GamePlatform platform;
	private Set<DownloadableFile> files;
	private Map<Long, VideoGame> games;

	public ScanGamesFolderExecutor( ScanGamesFolderTask task, DownloadableUtilsDAO downloadableDAO ) {
		super(task);
		this.platform = task.getPlatform();
		
		files = DownloadableManager.getInstance().getAllFiles(VideoGame.class);
		List<VideoGame> downloadedGames = GamesManager.getInstance().getGames( null, DownloadableStatus.DOWNLOADED );
		
		games = new HashMap<>();
		for (VideoGame videoGame : downloadedGames) {
			games.put( videoGame.getTheGamesDbId(), videoGame );
		}
	}
	
	private TheGamesDBGame selectGame( List<TheGamesDBGame> games, String title ) {
		
		if (games != null && games.size() > 0) {
			title = title.replaceAll("\\W", "").toUpperCase();
			for (TheGamesDBGame theGamesDBGame : games) {
				String gameTitle = theGamesDBGame.getGameTitle().replaceAll("\\W", "").toUpperCase();
				if (title.equals( gameTitle)) {
					return theGamesDBGame;
				}
			}
			return games.get(0);
		}
		
		return null;
		
	}

	public void parseFolder( Path folder ) throws IOException {
		
		TheGamesDBGame currentGame = null;

		if (!folder.equals(task.getFolder())) {
			String title = folder.getFileName().toString();
			
			title = title.replaceAll("\\_", " ");
			title = title.replaceAll("\\s+", " ");
			
			GetGamesListResponse gamesList = TheGamesDB.getInstance().getGamesList(title, platform.getLabel(), null);
			currentGame = selectGame( gamesList.getGames(), title );
		}
		
		DirectoryStream<Path> ds = Files.newDirectoryStream(folder, new Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				return Files.isDirectory(entry) || ( getFileFilter() == null || getFileFilter().accept(entry));
			}
		});
		for (Path p : ds) {
			if (Files.isDirectory(p)) {
				parseFolder(p);
			} else {
				handleFile( p, currentGame );
			}
		}
	}

	public void handleFile( Path p, TheGamesDBGame currentGame ) {

		Path videoGamePath = p.toAbsolutePath();
		for (DownloadableFile file : files) {
			if (file.getFilePath().equals( videoGamePath )) {
				return;
			}
		}

		int fileIndex = 0;

		String title = videoGamePath.getFileName().toString();

		LOGGER.debug(String.format("Identifying game from file : %s", videoGamePath.toAbsolutePath().toString()));

		if (Files.isRegularFile( videoGamePath ) && title.lastIndexOf('.') > 0 ) {
			title = title.substring(0, title.lastIndexOf('.'));	// remove file extension
		}

		String discNumberStr = RegExp.extract( title, ".*\\s+Dis[ck] (\\d+).*");
		if (discNumberStr != null) {
			fileIndex = Integer.parseInt( discNumberStr ) - 1;
			title = RegExp.extract( title, "(.*)\\s+Dis[ck] \\d+.*" );
		}

		if (currentGame == null) {
			GetGamesListResponse gamesList = TheGamesDB.getInstance().getGamesList(title, platform.getLabel(), null);
			currentGame = selectGame( gamesList.getGames(), title );
		}
		
		if (currentGame != null) {
			try {
				VideoGame game = games.get( currentGame.getId() );
				if (game == null) {
					game = GamesManager.getInstance().createGame(title, platform.getLabel(), currentGame.getId(), DownloadableStatus.DOWNLOADED);
					games.put( currentGame.getId(), game );
				}
				DownloadableManager.getInstance().addFile(game, p, fileIndex);
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
	}

	@Override
	public void parsePath(Path p) throws IOException {
		if (Files.isDirectory(p)) {
			parseFolder(p);
		} else {
			handleFile( p, null );
		}
	}

	@Override
	public Filter<Path> getFileFilter() {
		return platform.getFileFilter();
	}

}
