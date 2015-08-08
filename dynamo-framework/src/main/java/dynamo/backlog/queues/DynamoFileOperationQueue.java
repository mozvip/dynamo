package dynamo.backlog.queues;

import dynamo.core.model.AbstractDynamoQueue;

public class DynamoFileOperationQueue extends AbstractDynamoQueue {
	
	@Override
	public String getQueueName() {
		return "File operations queue";
	}

	public DynamoFileOperationQueue() {
		super(1);
	}

}
