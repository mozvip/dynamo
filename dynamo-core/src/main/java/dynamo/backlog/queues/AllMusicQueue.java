package dynamo.backlog.queues;

import dynamo.core.model.AbstractDynamoQueue;

public class AllMusicQueue extends AbstractDynamoQueue {
	
	@Override
	public String getQueueName() {
		return "AllMusic metadata download queue";
	}

	public AllMusicQueue() {
		super(2);
	}

}
