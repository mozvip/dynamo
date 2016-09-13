package dynamo.backlog.tasks.torrent;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.DownloadableManager;
import dynamo.torrent.parser.TorrentFile;
import dynamo.torrent.parser.TorrentProcessor;
import dynamo.torrents.transmission.Transmission;
import hclient.HTTPClient;

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
		if (filePath == null && task.getURL() != null) {
			filePath = Files.createTempFile("dynamo", ".torrent");
			String contentType = HTTPClient.getInstance().downloadToFile(task.getURL(), filePath, 0);
			
			if (StringUtils.equals(contentType, "text/html")) {
				
			}
			
		}
		
		String ident = null;

		if (filePath != null) {
			try (InputStream input = Files.newInputStream( filePath )) {
				TorrentFile torrentFile = TorrentProcessor.getTorrentFile( input );
				if (torrentFile == null) {
					throw new Exception("Downloaded file is not a torrent, will retry downloading it later");
				} else {
					if ( task.getDownloadable() != null) {
						int i = 0;
						for (String fileName : torrentFile.fileNames) {
							long size = torrentFile.length.get( i );
							searchResultDAO.createFile(fileName, size, task.getSearchResult().getUrl() );
							i++;
						}
					}
					
					ident = handleTorrent( filePath );
				}				
			}

		} else {
			
			// this has to be a magnet link
			if (Transmission.getInstance().isEnabled()) {
				long id = Transmission.getInstance().downloadByURL( task.getURL().getUrl() );
				if (id >= 0) {
					ident = "" + Transmission.getInstance().downloadByURL( task.getURL().getUrl() );
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
			item.setMinDate( getNextDate( 60 ) );
			BackLogProcessor.getInstance().schedule( item, true );
		}
	}

}
