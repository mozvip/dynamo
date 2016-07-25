package dynamo.backlog.tasks.movies;

import dynamo.core.model.DaemonTask;
import dynamo.movies.model.MovieManager;
import dynamo.trakt.TraktManager;

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
