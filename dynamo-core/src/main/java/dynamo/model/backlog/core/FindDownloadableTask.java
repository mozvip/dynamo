package dynamo.model.backlog.core;

import dynamo.core.model.Task;
import dynamo.model.Downloadable;

public abstract class FindDownloadableTask<T extends Downloadable> extends Task {
	
	protected T downloadable = null;

	public FindDownloadableTask( T downloadable ) {
		this.downloadable = downloadable;
	}
		
	@Override
	public String toString() {
		return String.format("Searching for %s", downloadable.toString());
	}

	public T getDownloadable() {
		return downloadable;
	}

}
