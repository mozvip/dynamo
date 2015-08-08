package dynamo.backlog.tasks.core;

import dynamo.core.model.InitTask;

public class RefreshWantedTask extends InitTask {
	
	@Override
	public String toString() {
		return "Queue search for wanted media";
	}

}
