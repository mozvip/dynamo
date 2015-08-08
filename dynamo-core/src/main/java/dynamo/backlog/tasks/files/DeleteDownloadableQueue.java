package dynamo.backlog.tasks.files;

import dynamo.core.model.AbstractDynamoQueue;

public class DeleteDownloadableQueue extends AbstractDynamoQueue {

	public DeleteDownloadableQueue() {
		super(20);
	}

	@Override
	public String getQueueName() {
		return "Delete Downloadable Queue";
	}

}
