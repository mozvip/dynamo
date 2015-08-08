package dynamo.core.model;

public class DynamoDefaultQueue extends AbstractDynamoQueue {

	public DynamoDefaultQueue() {
		super(4);
	}

	@Override
	public String getQueueName() {
		return "Dynamo default queue";
	}

}
