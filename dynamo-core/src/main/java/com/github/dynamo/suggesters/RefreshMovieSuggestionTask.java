package com.github.dynamo.suggesters;

import com.github.dynamo.core.model.DaemonTask;
import com.github.dynamo.movies.model.MovieManager;

public class RefreshMovieSuggestionTask extends DaemonTask {
	
	@Override
	public boolean isEnabled() {
		return MovieManager.getInstance().isEnabled();
	}

	public RefreshMovieSuggestionTask() {
		super( 24 * 60 );
	}

	@Override
	public String toString() {
		return "Refresh Movie suggestions";
	}

}
