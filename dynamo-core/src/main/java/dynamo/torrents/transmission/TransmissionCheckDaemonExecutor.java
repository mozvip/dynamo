package dynamo.torrents.transmission;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.Downloadable;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;

public class TransmissionCheckDaemonExecutor extends TaskExecutor<TransmissionCheckDaemonTask> implements Enableable {

	@Configurable(category="Torrents",
		name="Stop torrent when upload ratio reaches",  defaultValue="2.0", 
		required="#{dynamo:isActive('dynamo.torrents.transmission.DownloadTorrentTransmissionExecutor')}",
		disabled="#{!dynamo:isActive('dynamo.torrents.transmission.DownloadTorrentTransmissionExecutor')}")
	private float limitUploadRatio = 1.2f;

	public float getLimitUploadRatio() {
		return limitUploadRatio;
	}
	
	public void setLimitUploadRatio(float limitUploadRatio) {
		this.limitUploadRatio = limitUploadRatio;
	}
	
	@Override
	public boolean isEnabled() {
		return Transmission.getInstance().isEnabled();
	}
	
	private SearchResultDAO searchResultDAO;

	public TransmissionCheckDaemonExecutor(TransmissionCheckDaemonTask task, SearchResultDAO searchResultDAO) {
		super(task);
		this.searchResultDAO = searchResultDAO;
	}

	@Override
	public void execute() throws Exception {

		List<SearchResult> results = searchResultDAO.getActiveSearchResults( SearchResultType.TORRENT );
		List<TransmissionResponseTorrent> torrents = Transmission.getInstance().getTorrents();

		for (SearchResult searchResult : results) {
			boolean torrentFound = false;
			
			Downloadable downloadable = DownloadableFactory.getInstance().createInstance( searchResult.getDownloadableId() );
			if (downloadable == null) {
				continue;
			}

			for (TransmissionResponseTorrent torrent : torrents) {
				
				if (Integer.parseInt(searchResult.getClientId()) != torrent.getId()) {
					continue;
				}
				
				torrentFound = true;
				if (torrent.getDoneDate() > 0) {	
					Path sourceFolder = Paths.get( torrent.getDownloadDir() );
					List<Path> files = new ArrayList<>();
					for (ApiFile file : torrent.getFiles()) {
						if (file.getLength() > 0) {
							String fileName = file.getName().replace('?', '¿');
							files.add( sourceFolder.resolve(fileName) );
						}
					}
					// MAYBE_FIXME : sourceFolder is local to the transmission server, not to the dynamo server
					// This only works if transmission and dynamo are on the same server !
					DownloadableManager.getInstance().downloaded(task, downloadable, searchResult, sourceFolder, files, false );
					searchResultDAO.freeClientId(searchResult.getClientId());
				}
			}

			if (!torrentFound) {
				// torrent was deleted manually ?
				if (downloadable.isDownloaded()) {
					searchResultDAO.freeClientId(searchResult.getClientId());
				}
			}
		}
		
		List<SearchResult> blackListedResults = searchResultDAO.getBlackListedSearchResults( SearchResultType.TORRENT );
		for (SearchResult searchResult : blackListedResults) {
			if (searchResult.getClientId() != null) {
				for (TransmissionResponseTorrent torrent : torrents) {
					if (Integer.parseInt(searchResult.getClientId()) == torrent.getId()) {
						Transmission.getInstance().remove( torrent.getId() );
						searchResultDAO.freeClientId(searchResult.getClientId());
						break;
					}
				}
			}
		}
		
		for (TransmissionResponseTorrent torrent : torrents) {
			if (torrent.getDoneDate() > 0 && torrent.getUploadRatio() > limitUploadRatio) {
				// delete the files : FIXME : how to check that we have no file copy or move in progress ?
				searchResultDAO.freeClientId("" + torrent.getId());
				Transmission.getInstance().remove( torrent.getId() );
			}
		}
		
	}

}
