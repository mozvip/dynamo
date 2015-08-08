package dynamo.backlog.tasks.tvshows;

import dynamo.core.model.InitTask;
import dynamo.model.tvshows.TVShowManager;

public class TVShowsCleanupTask extends InitTask {
	
	@Override
	public boolean isEnabled() {
		return TVShowManager.getInstance().isEnabled();
	}
	
	@Override
	public String toString() {
		return "Cleanup TV Shows";
	}

}
