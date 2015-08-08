package dynamo.core.manager;

import dynamo.core.model.AbstractDynamoQueue;

public class LoggingQueue extends AbstractDynamoQueue {

	public LoggingQueue() {
		super(2);
	}

	@Override
	public String getQueueName() {
		return "Logging queue";
	}

}
