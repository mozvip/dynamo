package com.github.dynamo.webapps.sabnzbd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.files.DeleteEvent;
import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.FolderManager;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultType;
import com.github.mozvip.sabnzbd.model.SABHistoryResponse;
import com.github.mozvip.sabnzbd.model.SabNzbdResponseSlot;

public class SabNzbdCheckDaemonExecutor extends TaskExecutor<SabNzbdCheckDaemonTask> {

	private SearchResultDAO searchResultDAO;
	private static SabNzbd sab = SabNzbd.getInstance();

	public SabNzbdCheckDaemonExecutor(SabNzbdCheckDaemonTask task, SearchResultDAO searchResultDAO) {
		super(task);
		this.searchResultDAO = searchResultDAO;
	}

	@Override
	public void execute() throws Exception {
		
		sab.deleteFailed();
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
							files = FolderManager.getInstance().getAllFilesFrom( sourceFolder, true );
						}

						for (Iterator<Path> iterator = files.iterator(); iterator.hasNext();) {
							Path path = iterator.next();
							if (!Files.exists(path)) {
								iterator.remove();
							} else if (DownloadableManager.getInstance().isBlackListed(path, downloadable)) {
								iterator.remove();
								BackLogProcessor.getInstance().post( new DeleteEvent(path, false) );
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
