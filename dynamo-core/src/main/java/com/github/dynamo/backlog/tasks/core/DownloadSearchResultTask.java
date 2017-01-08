package com.github.dynamo.backlog.tasks.core;

import com.github.dynamo.core.model.Task;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.result.SearchResult;

public class DownloadSearchResultTask extends Task {
	
	private SearchResult searchResult;
	private Downloadable downloadable;
	
	public DownloadSearchResultTask(SearchResult searchResult, Downloadable downloadable) {
		this.searchResult = searchResult;
		this.downloadable = downloadable;
	}
	
	public SearchResult getSearchResult() {
		return searchResult;
	}

	public Downloadable getDownloadable() {
		return downloadable;
	}
	
	@Override
	public String toString() {
		return String.format("Downloading %s", searchResult.getTitle() ); 
	}

}
