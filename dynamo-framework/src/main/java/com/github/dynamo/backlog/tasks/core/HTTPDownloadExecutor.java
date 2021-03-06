package com.github.dynamo.backlog.tasks.core;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.WebResource;

public class HTTPDownloadExecutor extends TaskExecutor<HTTPDownloadTask> {

	public HTTPDownloadExecutor(HTTPDownloadTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		Path destinationFile = task.getDestinationFile().toAbsolutePath();
		if (!Files.isReadable( destinationFile ) || Files.size( destinationFile ) == 0) {
			String contentType = HTTPClient.getInstance().downloadToFile( new WebResource( task.getUrl(), task.getReferer() ), destinationFile, 0 );
			if (task.isImage() && !StringUtils.startsWith(contentType, "image/")) {
				ErrorManager.getInstance().reportError(String.format("%s is not an image", task.getUrl()));
				Files.delete( destinationFile );
			}
		}
	}
	
	@Override
	public void rescheduleTask(HTTPDownloadTask item) {
		if ( isFailed() ) {
			BackLogProcessor.getInstance().schedule(item, getNextDate( 60 ), false);
		}
	}

}
