package dynamo.model.backlog.subtitles;

import dynamo.core.model.AbstractDynamoQueue;

public class FindSubtitlesQueue extends AbstractDynamoQueue {

	public FindSubtitlesQueue() {
		super(2);
	}

	@Override
	public String getQueueName() {
		return "Find subtitles queue";
	}

}
