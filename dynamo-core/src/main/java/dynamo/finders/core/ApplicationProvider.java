package dynamo.finders.core;

import java.util.List;

import dynamo.core.model.ApplicationPlatform;
import dynamo.model.result.SearchResult;

public interface ApplicationProvider {
	
	public List<SearchResult> searchApplication( String name, ApplicationPlatform platform );

}
