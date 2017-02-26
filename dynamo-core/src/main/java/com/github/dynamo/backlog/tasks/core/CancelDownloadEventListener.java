package com.github.dynamo.backlog.tasks.core;

import java.io.IOException;
import java.util.EventListener;

import com.github.dynamo.backlog.tasks.torrent.Transmission;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.model.result.SearchResultType;
import com.github.dynamo.webapps.sabnzbd.SabNzbd;
import com.google.common.eventbus.Subscribe;

public class CancelDownloadEventListener implements EventListener {

	SearchResultDAO searchResultDAO = DAOManager.getInstance().getDAO( SearchResultDAO.class );

	@Subscribe
	public void execute( CancelDownloadEvent event ) throws NumberFormatException, IOException {
		// hackish : shouldn't be implemented like this
		if ( event.getResult().getType() == SearchResultType.TORRENT && Transmission.getInstance().isEnabled()) {
			Transmission.getInstance().remove( Long.parseLong( event.getResult().getClientId() ), true );
		}
		else if ( event.getResult().getType() == SearchResultType.NZB && SabNzbd.getInstance().isEnabled()) {
			SabNzbd.getInstance().remove( event.getResult().getClientId() );
		}
		searchResultDAO.freeClientId( event.getResult().getClientId() );
	}

}
