package com.github.dynamo.backlog.tasks.movies;

import com.github.dynamo.core.model.DaemonTask;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.trakt.TraktManager;

public class RefreshWatchedMoviesTask extends DaemonTask {
	
	@Override
	public boolean isEnabled() {
		return MovieManager.getInstance().isEnabled() && TraktManager.getInstance().isEnabled();
	}
	
	public RefreshWatchedMoviesTask() {
		super( 24 * 60);
	}

	@Override
	public String toString() {
		return "Refresh list of watched movies";
	}

}
