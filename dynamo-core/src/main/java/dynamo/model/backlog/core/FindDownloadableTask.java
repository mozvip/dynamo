package dynamo.model.backlog.core;

import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.CancellableTask;
import dynamo.core.model.Task;
import dynamo.manager.DownloadableManager;
import dynamo.model.Downloadable;

public abstract class FindDownloadableTask<T extends Downloadable> extends Task implements CancellableTask {
	
	protected T downloadable = null;

	public FindDownloadableTask( T downloadable ) {
		this.downloadable = downloadable;
	}
		
	@Override
	public String toString() {
		return String.format("Searching for %s", downloadable.toString());
	}
	
	@Override
	public Class<? extends AbstractDynamoQueue> getQueueClass() {
		return FindDownloadableQueue.class;
	}
	
	@Override
	public void cancel() {
		DownloadableManager.getInstance().ignore( downloadable );
	}
	
	public T getDownloadable() {
		return downloadable;
	}

}
