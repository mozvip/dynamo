package com.github.dynamo.backlog.tasks.core;

import com.github.dynamo.backlog.tasks.torrent.Transmission;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.model.result.SearchResultType;
import com.github.dynamo.webapps.sabnzbd.SabNzbd;

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
			Transmission.getInstance().remove( Long.parseLong( task.getResult().getClientId() ), true );
		}
		else if ( task.getResult().getType() == SearchResultType.NZB && SabNzbd.getInstance().isEnabled()) {
			SabNzbd.getInstance().remove( task.getResult().getClientId() );
		}
		searchResultDAO.freeClientId( task.getResult().getClientId() );
	}

}
