package dynamo.backlog.tasks.files;

import dynamo.backlog.queues.DynamoFileOperationQueue;
import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.DownloadableTask;
import dynamo.model.Downloadable;

public abstract class FileOperationBackLogItem extends DownloadableTask {

	public FileOperationBackLogItem(Downloadable downloadable) {
		super(downloadable);
	}

	@Override
	public Class<? extends AbstractDynamoQueue> getQueueClass() {
		return DynamoFileOperationQueue.class;
	}

}
