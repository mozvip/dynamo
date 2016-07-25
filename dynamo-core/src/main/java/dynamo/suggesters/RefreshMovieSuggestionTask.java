package dynamo.suggesters;

import dynamo.core.model.DaemonTask;
import dynamo.movies.model.MovieManager;

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
