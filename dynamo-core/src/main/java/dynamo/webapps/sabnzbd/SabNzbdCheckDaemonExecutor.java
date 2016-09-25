package dynamo.webapps.sabnzbd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.tasks.files.DeleteTask;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.FolderManager;
import dynamo.model.Downloadable;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;

public class SabNzbdCheckDaemonExecutor extends TaskExecutor<SabNzbdCheckDaemonTask> {

	private SearchResultDAO searchResultDAO;
	private static SabNzbd sab = SabNzbd.getInstance();

	public SabNzbdCheckDaemonExecutor(SabNzbdCheckDaemonTask task, SearchResultDAO searchResultDAO) {
		super(task);
		this.searchResultDAO = searchResultDAO;
	}

	@Override
	public void execute() throws Exception {

		SABHistoryResponse response = sab.getHistory();

		List<SearchResult> results = searchResultDAO.getActiveSearchResults( SearchResultType.NZB );
		for (SearchResult searchResult : results) {
			
			Downloadable downloadable = DownloadableFactory.getInstance().createInstance( searchResult.getDownloadableId() );

			Optional<SabNzbdResponseSlot> optSlot = response.getSlots().stream().filter( slot -> slot.getNzo_id().equals(searchResult.getClientId())).findFirst();

			if (optSlot.isPresent()) {
				
				SabNzbdResponseSlot slot = optSlot.get();
				
				boolean failed = StringUtils.equals(slot.getStatus(), "Failed");
				boolean completed = StringUtils.equals(slot.getStatus(), "Completed"); 
				
				if (!(failed || completed)) {
					continue;
				}
				
				if (failed) {
					sab.deleteFromHistory( slot.getNzo_id() );
				}
				
				if (downloadable == null) {
					searchResultDAO.deleteResultForDownloadableId( searchResult.getDownloadableId() );
					// TODO: remove files 
				}
				if (!downloadable.isDownloaded()) {
					
					if (failed) {
						DownloadableManager.getInstance().redownload( downloadable );
					} else {
					
						Path sourceFolder = Paths.get( slot.getStorage() ).toAbsolutePath();
						
						List<Path> files;
						if (!Files.isDirectory( sourceFolder )) {
							files = new ArrayList<>();
							files.add( sourceFolder );
							sourceFolder = sourceFolder.getParent();
						} else {
							files = FolderManager.getAllFilesFrom( sourceFolder, true );
						}

						// FIXME: sourceFolder is local to the SABnzbd server, not to the dynamo server !!!
						// This only works if SABnzbd and dynamo are on the same server !

						for (Iterator<Path> iterator = files.iterator(); iterator.hasNext();) {
							Path path = iterator.next();
							if (!Files.exists(path)) {
								iterator.remove();
							} else if (DownloadableManager.getInstance().isBlackListed(path, downloadable)) {
								iterator.remove();
								queue( new DeleteTask(path, false), false );
							}
						}

						if (files != null && files.size() > 0) {
							try {
								DownloadableManager.getInstance().downloaded(task, downloadable, searchResult, sourceFolder, files, true );
							} catch (Exception e) {
								ErrorManager.getInstance().reportThrowable(task, e);
							}
						} else {
							sab.deleteFromHistory( slot.getNzo_id() );
							DownloadableManager.getInstance().redownload( downloadable );
						}
					}
				}
				
			} else {
				// download is not present from queue, redownload
				DownloadableManager.getInstance().redownload( downloadable );
			}
		}
	}

}
