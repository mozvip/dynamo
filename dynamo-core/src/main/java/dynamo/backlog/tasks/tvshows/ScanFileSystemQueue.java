package dynamo.backlog.tasks.tvshows;

import dynamo.core.model.AbstractDynamoQueue;

public class ScanFileSystemQueue extends AbstractDynamoQueue {

	public ScanFileSystemQueue() {
		super(2);
	}

	@Override
	public String getQueueName() {
		return "Scan File System Queue";
	}

}
