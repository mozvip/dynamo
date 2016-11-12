package dynamo.backlog.tasks.tvshows;

import dynamo.core.model.AbstractDynamoQueue;

public class FileSystemQueue extends AbstractDynamoQueue {

	public FileSystemQueue() {
		super(80);
	}

	@Override
	public String getQueueName() {
		return "Scan File System Queue";
	}

}
