package dynamo.suggesters;

import java.util.HashSet;
import java.util.Set;

import com.omertron.thetvdbapi.model.Series;

import dynamo.core.model.TaskExecutor;

public class RefreshTVShowSuggestionsExecutor extends TaskExecutor<RefreshTVShowSuggestionsTask> {

	public RefreshTVShowSuggestionsExecutor(RefreshTVShowSuggestionsTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {

		Set<Series> suggestions = new HashSet<Series>();

		// TODO
		
	}

}
