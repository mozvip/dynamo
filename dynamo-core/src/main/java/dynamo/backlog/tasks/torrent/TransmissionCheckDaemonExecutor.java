package dynamo.backlog.tasks.torrent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.FileOperationTask;
import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import fr.mozvip.transmission.model.Torrent;
import fr.mozvip.transmission.model.TorrentFile;

public class TransmissionCheckDaemonExecutor extends TaskExecutor<TransmissionCheckDaemonTask> implements Enableable {

	@Configurable(ifExpression="dynamo.torrents.transmission.DownloadTorrentTransmissionExecutor", defaultValue="2.0")
	private float limitUploadRatio = 2.0f;

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
	
	private List<Path> getSourceFiles( Torrent torrent ) {
		Path sourceFolder = Paths.get( torrent.getDownloadDir() );
		List<Path> files = new ArrayList<>();
		for (TorrentFile file : torrent.getFiles()) {
			if (file.getLength() > 0) {
				String fileName = file.getName().replace('?', '¿');
				files.add( sourceFolder.resolve(fileName) );
			}
		}
		return files;
	}

	@Override
	public void execute() throws Exception {

		List<SearchResult> results = searchResultDAO.getActiveSearchResults( SearchResultType.TORRENT );
		Transmission transmission = Transmission.getInstance();
		List<Torrent> torrents = transmission.getTorrents();

		for (SearchResult searchResult : results) {
			boolean torrentFound = false;
			
			Downloadable downloadable = DownloadableFactory.getInstance().createInstance( searchResult.getDownloadableId() );
			if (downloadable == null) {
				searchResultDAO.deleteResultForDownloadableId( searchResult.getDownloadableId() );
				continue;
			}

			for (Torrent torrent : torrents) {
				
				if (Integer.parseInt(searchResult.getClientId()) != torrent.getId()) {
					continue;
				}
				
				torrentFound = true;
				if (torrent.getDoneDate() > 0) {	
					List<Path> files = getSourceFiles( torrent );

					boolean move = false;
					if (torrent.getUploadRatio() >= limitUploadRatio) {
						move = true;
						transmission.remove( torrent.getId(), false );
					}

					Path downloadFolder = Paths.get( torrent.getDownloadDir() );
					DownloadableManager.getInstance().downloaded(task, downloadable, searchResult, downloadFolder, files, move );
				}
			}

			if (!torrentFound) {
				// torrent was deleted manually ?
				searchResultDAO.freeClientId(searchResult.getClientId());
				if ( downloadable.getStatus() == DownloadableStatus.SNATCHED){
					DownloadableManager.getInstance().blackListSearchResult( searchResult.getUrl() );
					DownloadableManager.getInstance().want(downloadable);
				}
			}
		}
		
		List<SearchResult> blackListedResults = searchResultDAO.getBlackListedSearchResults( SearchResultType.TORRENT );
		for (SearchResult searchResult : blackListedResults) {
			for (Torrent torrent : torrents) {
				if (Integer.parseInt(searchResult.getClientId()) == torrent.getId()) {
					transmission.remove( torrent.getId(), true );
					searchResultDAO.freeClientId(searchResult.getClientId());
					break;
				}
			}
		}
		
		if (BackLogProcessor.getInstance().isRunningOrPending( FileOperationTask.class )) {
			// do not cancel any download if file operations are in progress
			return;
		}

		for (Torrent torrent : torrents) {
			if (torrent.getDoneDate() > 0 && torrent.getUploadRatio() >= limitUploadRatio) {
				
				
				// delete the torrent and associated files : TODO 
				searchResultDAO.freeClientId("" + torrent.getId());
				transmission.remove( torrent.getId(), true );
			}
		}
		
	}

}
