package dynamo.backlog.tasks.core;

import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.model.result.SearchResultType;
import dynamo.torrents.transmission.Transmission;
import dynamo.webapps.sabnzbd.SabNzbd;

public class CancelDownloadTaskExecutor extends TaskExecutor<CancelDownloadTask> {

	public CancelDownloadTaskExecutor(CancelDownloadTask task, SearchResultDAO searchResultDAO) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		// hackish : shouldn't be implemented like this
		if ( task.getResult().getType() == SearchResultType.TORRENT && Transmission.getInstance().isEnabled()) {
			Transmission.getInstance().remove( Integer.parseInt( task.getResult().getClientId() ) );
		}
		
		else if ( task.getResult().getType() == SearchResultType.NZB && SabNzbd.getInstance().isEnabled()) {
			
			SabNzbd.getInstance().remove( task.getResult().getClientId() );
			
		}
	}

}
