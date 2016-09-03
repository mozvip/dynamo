package dynamo.backlog.tasks.core;

import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.model.result.SearchResultType;
import dynamo.torrents.transmission.Transmission;
import dynamo.webapps.sabnzbd.SabNzbd;

public class CancelDownloadTaskExecutor extends TaskExecutor<CancelDownloadTask> {
	
	SearchResultDAO searchResultDAO;

	public CancelDownloadTaskExecutor(CancelDownloadTask task, SearchResultDAO searchResultDAO) {
		super(task);
		this.searchResultDAO = searchResultDAO;
	}

	@Override
	public void execute() throws Exception {
		// hackish : shouldn't be implemented like this
		if ( task.getResult().getType() == SearchResultType.TORRENT && Transmission.getInstance().isEnabled()) {
			Transmission.getInstance().remove( Integer.parseInt( task.getResult().getClientId() ), true );
		}
		else if ( task.getResult().getType() == SearchResultType.NZB && SabNzbd.getInstance().isEnabled()) {
			SabNzbd.getInstance().remove( task.getResult().getClientId() );
		}
		searchResultDAO.freeClientId( task.getResult().getClientId() );
	}

}
