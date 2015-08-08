package dynamo.backlog.tasks.music;

import dynamo.core.model.InitTask;
import dynamo.manager.MusicManager;

public class SynchronizeTagsTask extends InitTask {
	
	@Override
	public boolean isEnabled() {
		return MusicManager.getInstance().isEnabled();
	}

	@Override
	public String toString() {
		return "Synchronize music tags";
	}

}
