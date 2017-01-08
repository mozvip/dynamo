package com.github.dynamo.backlog.tasks.core;

import java.nio.file.Path;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.nzb.DownloadNZBTask;
import com.github.dynamo.backlog.tasks.torrent.DownloadTorrentTask;
import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultType;

public class DownloadSearchResultExecutor extends TaskExecutor<DownloadSearchResultTask> {

	public DownloadSearchResultExecutor(DownloadSearchResultTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {

		SearchResult result = task.getSearchResult();

		if (result.getUrl().startsWith("http") ) {
			DownloadFinder finder = DynamoObjectFactory.getInstance( task.getSearchResult().getProviderClass() );
			// download the file locally
			Path filePath = finder.download( result.getUrl(), result.getReferer() );
			if (filePath != null) {
				if ( result.getType() == SearchResultType.TORRENT) {
					BackLogProcessor.getInstance().schedule( new DownloadTorrentTask(filePath, result, task.getDownloadable()), false);
				} else if ( result.getType() == SearchResultType.NZB ) {
					BackLogProcessor.getInstance().schedule( new DownloadNZBTask(filePath, result, task.getDownloadable()), false);
				}
			} else {
				// blacklist this result : the URL can't be retrieved
				DownloadableManager.getInstance().blackListSearchResult( result.getUrl() );
				DownloadableManager.getInstance().want( task.getDownloadable() );
			}

		} else if (result.getUrl().startsWith("magnet")) {

			BackLogProcessor.getInstance().schedule( new DownloadTorrentTask( result.getUrl(), result, task.getDownloadable()), false );
			
		} else {

			ErrorManager.getInstance().reportError( task, String.format("Unsupported protocol for url : %s", result.getUrl()) );
			
		}

	}
	
	@Override
	public void rescheduleTask(DownloadSearchResultTask taskToReschedule) {
		if (isFailed()) {
			BackLogProcessor.getInstance().schedule(taskToReschedule, getNextDate(30), false);
		}
	}

}
