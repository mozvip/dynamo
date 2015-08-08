package dynamo.backlog.tasks.tvshows;

import dynamo.core.model.AbstractDynamoQueue;

public class ScanFolderQueue extends AbstractDynamoQueue {

	public ScanFolderQueue() {
		super(1);
	}

	@Override
	public String getQueueName() {
		return "Scan Filesystem folder Queue";
	}

}
