package com.github.dynamo.suggesters;

import com.github.dynamo.core.model.DaemonTask;
import com.github.dynamo.manager.MusicManager;

public class RefreshMusicSuggestionsTask extends DaemonTask {
	
	@Override
	public boolean isEnabled() {
		return MusicManager.getInstance().isEnabled();
	}

	public RefreshMusicSuggestionsTask() {
		super( 24 * 60 );
	}

	@Override
	public String toString() {
		return "Refresh Music suggestions";
	}

}
