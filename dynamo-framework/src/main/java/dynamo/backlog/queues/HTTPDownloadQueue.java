package dynamo.backlog.queues;

import dynamo.core.model.AbstractDynamoQueue;

public class HTTPDownloadQueue extends AbstractDynamoQueue {
	
	public HTTPDownloadQueue() {
		super(4);
	}

	@Override
	public String getQueueName() {
		return "HTTP downloads queue";
	}

}
