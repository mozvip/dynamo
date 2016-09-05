package dynamo.server;

import dynamo.core.DynamoApplication;

public class Dynamo extends DynamoApplication {

	public Dynamo() throws Exception {
		super();
	}

	@Override
	protected String getApplicationName() {
		return "Dynamo";
	}

	public static void main(String[] args) throws Exception {
		new Dynamo().init();
	}

}
