package com.github.dynamo.backlog.tasks.movies;

import com.github.dynamo.core.model.DaemonTask;
import com.github.dynamo.movies.model.MovieManager;

public class MovieCleanupTask extends DaemonTask {
	
	@Override
	public boolean isEnabled() {
		return MovieManager.getInstance().isEnabled();
	}

	public MovieCleanupTask() {
		super( 24 * 60 );
	}

	@Override
	public String toString() {
		return "Clean up movie collection";
	}

}
