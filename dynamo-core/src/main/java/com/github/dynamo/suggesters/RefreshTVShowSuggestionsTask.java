package com.github.dynamo.suggesters;

import com.github.dynamo.core.model.DaemonTask;
import com.github.dynamo.tvshows.model.TVShowManager;

public class RefreshTVShowSuggestionsTask extends DaemonTask {
	
	@Override
	public boolean isEnabled() {
		return TVShowManager.getInstance().isEnabled();
	}

	public RefreshTVShowSuggestionsTask() {
		super( 24 * 60 );
	}

	@Override
	public String toString() {
		return "Refresh TV Show suggestions";
	}


}
