package dynamo.backlog.tasks.movies;

import dynamo.core.model.DaemonTask;
import dynamo.model.tvshows.TVShowManager;
import dynamo.trakt.TraktManager;

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
