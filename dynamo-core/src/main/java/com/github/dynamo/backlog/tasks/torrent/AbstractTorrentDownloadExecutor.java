package com.github.dynamo.backlog.tasks.torrent;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.torrent.parser.TorrentFile;
import com.github.dynamo.torrent.parser.TorrentProcessor;

public abstract class AbstractTorrentDownloadExecutor extends TaskExecutor<DownloadTorrentTask> {

	private SearchResultDAO searchResultDAO;

	public AbstractTorrentDownloadExecutor(DownloadTorrentTask task, SearchResultDAO searchResultDAO) {
		super(task);
		this.searchResultDAO = searchResultDAO;
	}
	
	public abstract String handleTorrent( Path torrentFilePath ) throws Exception;

	@Override
	public void execute() throws Exception {
	
		Path filePath = task.getTorrentFilePath();
		if (filePath == null || !Files.exists(filePath)) {
			if (task.getURL().getUrl().startsWith("http")) {
				filePath = Files.createTempFile("dynamo", ".torrent");
			}
		}
		
		String ident = null;

		if (filePath != null && Files.exists(filePath)) {
			try (InputStream input = Files.newInputStream( filePath )) {
				TorrentFile torrentFile = TorrentProcessor.getTorrentFile( input );
				if (torrentFile == null) {
					DownloadableManager.getInstance().blackListSearchResult( task.getSearchResult().getUrl() );
					throw new Exception("Downloaded file is not a torrent, blacklisting URL");
				} else {
					ident = handleTorrent( filePath );
				}
			} finally {
				Files.deleteIfExists( filePath );
			}

		} else {

			if (Transmission.getInstance().isEnabled()) {
				long id = Transmission.getInstance().downloadByUrl( task.getURL().getUrl() );
				if (id >= 0) {
					ident = "" + id;
				} else {
					throw new Exception( String.format("Could not download torrent at url %s", task.getURL() ) );
				}
			} else {
				throw new Exception( String.format("Could not download torrent at url %s with current setup", task.getURL() ) );
			}
			
		}

		DownloadableManager.getInstance().snatched( task.getDownloadable(), task.getSearchResult() );
		if (ident != null && !ident.equals(task.getSearchResult().getClientId())) {
			searchResultDAO.freeClientId( ident );
			searchResultDAO.updateClientId( task.getSearchResult().getUrl(), ident );
		}
	}
	
	@Override
	public void rescheduleTask(DownloadTorrentTask item) {
		if (isFailed()) {
			BackLogProcessor.getInstance().schedule( item, getNextDate( 60 ), false );
		}
	}

}
