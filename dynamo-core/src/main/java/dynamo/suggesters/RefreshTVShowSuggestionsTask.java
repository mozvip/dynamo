package dynamo.suggesters;

import dynamo.core.model.DaemonTask;
import dynamo.model.tvshows.TVShowManager;

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
