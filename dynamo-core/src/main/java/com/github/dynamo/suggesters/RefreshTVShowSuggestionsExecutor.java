package com.github.dynamo.suggesters;

import java.util.HashSet;
import java.util.Set;

import com.github.dynamo.core.model.TaskExecutor;
import com.omertron.thetvdbapi.model.Series;

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
