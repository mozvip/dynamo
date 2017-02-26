package com.github.dynamo.backlog.tasks.files;

import java.nio.file.Files;
import java.util.EventListener;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.google.common.eventbus.Subscribe;

public class DeleteFileEventListener implements EventListener {
	
	private static DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );

	@Subscribe
	public void execute( DeleteFileEvent event ) {
		downloadableDAO.deleteFile( event.getPath() );
		if (Files.exists( event.getPath() )) {
			BackLogProcessor.getInstance().post( new DeleteEvent( event.getPath(), false ));
		}
	}

}
