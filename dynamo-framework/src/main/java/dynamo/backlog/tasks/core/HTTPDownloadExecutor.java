package dynamo.backlog.tasks.core;

import java.nio.file.Files;
import java.nio.file.Path;

import core.WebResource;
import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.TaskExecutor;
import dynamo.model.backlog.core.HTTPDownloadTask;
import hclient.HTTPClient;

public class HTTPDownloadExecutor extends TaskExecutor<HTTPDownloadTask> {

	public HTTPDownloadExecutor(HTTPDownloadTask item) {
		super(item);
	}

	@Override
	public void execute() throws Exception {
		Path destinationFile = task.getDestinationFile().toAbsolutePath();
		if (!Files.isReadable( destinationFile ) || Files.size( destinationFile ) == 0) {
			HTTPClient.getInstance().downloadToFile( new WebResource( task.getUrl(), task.getReferer() ), destinationFile, 0 );
		}
	}
	
	@Override
	public void rescheduleTask(HTTPDownloadTask item) {
		if ( isFailed() ) {
			item.setMinDate( getNextDate( 60 ) );
			BackLogProcessor.getInstance().schedule(item, false);
		}
	}

}
