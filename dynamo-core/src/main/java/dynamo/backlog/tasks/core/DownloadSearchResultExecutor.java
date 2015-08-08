package dynamo.backlog.tasks.core;

import java.nio.file.Path;

import dynamo.backlog.tasks.nzb.DownloadNZBTask;
import dynamo.backlog.tasks.torrent.DownloadTorrentTask;
import dynamo.core.DownloadFinder;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;

public class DownloadSearchResultExecutor extends TaskExecutor<DownloadSearchResultTask> {

	public DownloadSearchResultExecutor(DownloadSearchResultTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {

		SearchResult result = task.getSearchResult();

		if (result.getUrl().startsWith("http") ) {
			DownloadFinder finder = (DownloadFinder) DynamoObjectFactory.getInstance( task.getSearchResult().getProviderClass() );
			// download the file locally
			Path filePath = finder.download( result.getUrl(), task.getSearchResult().getReferer() );
			if (filePath != null) {
				if ( result.getType() == SearchResultType.TORRENT) {
					queue( new DownloadTorrentTask(filePath, result, task.getDownloadable()), false);
				} else if ( result.getType() == SearchResultType.NZB ) {
					queue( new DownloadNZBTask(filePath, result, task.getDownloadable()), false);
				}
			} else {
				// blacklist this result : the URL can't be retrieved
				DownloadableManager.getInstance().blackListSearchResult( result.getUrl() );
				DownloadableManager.getInstance().want( task.getDownloadable() );
			}

		} else if (result.getUrl().startsWith("magnet")) {

			queue( new DownloadTorrentTask( result.getUrl(), result, task.getDownloadable()), false );
			
		} else {

			ErrorManager.getInstance().reportError( task, String.format("Unsupported protocol for url : %s", result.getUrl()) );
			
		}

	}
	
	@Override
	public void rescheduleTask(DownloadSearchResultTask taskToReschedule) {
		if (isFailed()) {
			taskToReschedule.setMinDate( getNextDate(30) );
			queue(taskToReschedule);
		}
	}

}
