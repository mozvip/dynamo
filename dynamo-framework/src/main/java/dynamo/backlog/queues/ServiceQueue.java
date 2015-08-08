package dynamo.backlog.queues;

import dynamo.core.model.AbstractDynamoQueue;

public class ServiceQueue extends AbstractDynamoQueue {

	public ServiceQueue() {
		super(100);
	}

	@Override
	public String getQueueName() {
		return "Services Queue";
	}

}
