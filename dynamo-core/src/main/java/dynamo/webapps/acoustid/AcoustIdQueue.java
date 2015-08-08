package dynamo.webapps.acoustid;

import dynamo.core.model.AbstractDynamoQueue;

public class AcoustIdQueue extends AbstractDynamoQueue {

	public AcoustIdQueue() {
		super(1);
	}

	@Override
	public String getQueueName() {
		return "AcoustID.org web service queue";
	}

}
