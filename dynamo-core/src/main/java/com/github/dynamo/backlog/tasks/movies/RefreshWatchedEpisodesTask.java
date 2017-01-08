package com.github.dynamo.backlog.tasks.movies;

import com.github.dynamo.core.model.DaemonTask;
import com.github.dynamo.trakt.TraktManager;
import com.github.dynamo.tvshows.model.TVShowManager;

public class RefreshWatchedEpisodesTask extends DaemonTask {
	
	@Override
	public boolean isEnabled() {
		return TVShowManager.getInstance().isEnabled() && TraktManager.getInstance().isEnabled();
	}
	
	public RefreshWatchedEpisodesTask() {
		super( 24 * 60);
	}

	@Override
	public String toString() {
		return "Refresh list of watched episodes";
	}	

}
