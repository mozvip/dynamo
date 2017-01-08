package com.github.dynamo.magazines;

import java.util.List;

import com.github.dynamo.model.result.SearchResult;

public interface MagazineProvider {
	
	public List<SearchResult> findDownloadsForMagazine( String issueSearchString ) throws Exception;

}
