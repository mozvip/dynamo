package dynamo.backlog.tasks.core;

import dynamo.core.model.Task;
import dynamo.model.Downloadable;

public abstract class FindDownloadableImageTask<T extends Downloadable> extends Task {
	
	private T downloadable;
	
	public FindDownloadableImageTask( T downloadable ) {
		this.downloadable = downloadable;
	}
	
	public T getDownloadable() {
		return downloadable;
	}

}
