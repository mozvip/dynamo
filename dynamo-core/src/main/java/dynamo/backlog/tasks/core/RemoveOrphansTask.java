package dynamo.backlog.tasks.core;

import dynamo.core.model.InitTask;

public class RemoveOrphansTask extends InitTask {
	
	@Override
	public String toString() {
		return "Remove oprhans from DB";
	}

}
