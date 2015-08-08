package dynamo.magazines;

import java.util.List;

import dynamo.model.result.SearchResult;

public interface MagazineProvider {
	
	public List<SearchResult> findDownloadsForMagazine( String issueSearchString ) throws Exception;

}
