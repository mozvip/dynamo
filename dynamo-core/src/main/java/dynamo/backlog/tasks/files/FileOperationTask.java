package dynamo.backlog.tasks.files;

import dynamo.backlog.queues.DynamoFileOperationQueue;
import dynamo.core.DynamoTask;
import dynamo.core.model.DownloadableTask;
import dynamo.model.Downloadable;

@DynamoTask(queueClass=DynamoFileOperationQueue.class)
public abstract class FileOperationTask extends DownloadableTask {

	public FileOperationTask(Downloadable downloadable) {
		super(downloadable);
	}

}
