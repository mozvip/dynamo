package com.github.dynamo.backlog.tasks.core;

import com.github.dynamo.model.result.SearchResult;

public class CancelDownloadEvent {

	private SearchResult result;

	public CancelDownloadEvent( SearchResult result ) {
		super();
		this.result = result;
	}
	
	public SearchResult getResult() {
		return result;
	}
	
	@Override
	public String toString() {
		return String.format( "Canceling download %s", result.getTitle() );
	}
	

}
