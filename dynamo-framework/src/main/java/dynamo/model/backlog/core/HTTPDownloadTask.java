package dynamo.model.backlog.core;

import java.nio.file.Path;

import dynamo.backlog.queues.HTTPDownloadQueue;
import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.Task;

public class HTTPDownloadTask extends Task {
	
	private String url;
	private String referer;
	private Path destinationFile;

	public HTTPDownloadTask(String url, String referer, Path destinationFile ) {
		this.url = url;
		this.referer = referer;
		this.destinationFile = destinationFile;
	}

	public String getUrl() {
		return url;
	}
	
	public String getReferer() {
		return referer;
	}

	public Path getDestinationFile() {
		return destinationFile;
	}

	@Override
	public String toString() {
		return String.format( "Downloading <a href='%s'>%s</a> to %s", url, url, destinationFile.toAbsolutePath().toString() );
	}
	
	@Override
	public Class<? extends AbstractDynamoQueue> getQueueClass() {
		return HTTPDownloadQueue.class;
	}	

}
