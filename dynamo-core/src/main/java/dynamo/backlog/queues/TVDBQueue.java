package dynamo.backlog.queues;

import dynamo.core.model.AbstractDynamoQueue;

public class TVDBQueue extends AbstractDynamoQueue {
	
	@Override
	public String getQueueName() {
		return "TVDB metadata queue";
	}

	public TVDBQueue() {
		super(3);
	}

}
