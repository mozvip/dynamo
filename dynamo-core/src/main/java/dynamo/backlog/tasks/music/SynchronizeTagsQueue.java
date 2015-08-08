package dynamo.backlog.tasks.music;

import dynamo.core.model.AbstractDynamoQueue;

public class SynchronizeTagsQueue extends AbstractDynamoQueue {

	public SynchronizeTagsQueue() {
		super(1);
	}

	@Override
	public String getQueueName() {
		return "Music Tags Synchronization Queue";
	}

}
