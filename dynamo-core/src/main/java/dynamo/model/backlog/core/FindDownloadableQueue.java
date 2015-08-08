package dynamo.model.backlog.core;

import dynamo.core.model.AbstractDynamoQueue;

public class FindDownloadableQueue extends AbstractDynamoQueue {

	public FindDownloadableQueue() {
		super(2);
	}

	@Override
	public String getQueueName() {
		return "Find Downloads Queue";
	}

}
