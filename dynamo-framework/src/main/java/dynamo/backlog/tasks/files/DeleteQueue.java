package dynamo.backlog.tasks.files;

import dynamo.core.model.AbstractDynamoQueue;

public class DeleteQueue extends AbstractDynamoQueue {

	public DeleteQueue() {
		super( 4 );
	}

	@Override
	public String getQueueName() {
		return "Delete files on the file system";
	}

}
