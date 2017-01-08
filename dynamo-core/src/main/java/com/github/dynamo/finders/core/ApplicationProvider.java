package com.github.dynamo.finders.core;

import java.util.List;

import com.github.dynamo.core.model.ApplicationPlatform;
import com.github.dynamo.model.result.SearchResult;

public interface ApplicationProvider {
	
	public List<SearchResult> searchApplication( String name, ApplicationPlatform platform );

}
