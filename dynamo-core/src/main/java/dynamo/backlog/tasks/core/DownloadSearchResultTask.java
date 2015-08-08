package dynamo.backlog.tasks.core;

import dynamo.core.model.Task;
import dynamo.model.Downloadable;
import dynamo.model.result.SearchResult;

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
