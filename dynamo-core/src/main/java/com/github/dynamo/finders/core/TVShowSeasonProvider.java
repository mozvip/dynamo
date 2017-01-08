package com.github.dynamo.finders.core;

import java.util.List;

import com.github.dynamo.core.Language;
import com.github.dynamo.model.result.SearchResult;

public interface TVShowSeasonProvider {

	List<SearchResult> findDownloadsForSeason( String aka, Language audioLanguage, int seasonNumber ) throws Exception;

}
