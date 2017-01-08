package com.github.dynamo.backlog.tasks.music;

import com.github.dynamo.core.model.InitTask;
import com.github.dynamo.manager.MusicManager;

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
