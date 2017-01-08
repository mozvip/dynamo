package com.github.dynamo.finders.core;

import java.util.List;

import com.github.dynamo.core.Language;
import com.github.dynamo.model.result.SearchResult;

public interface EpisodeFinder {
	
	public List<SearchResult> findEpisode( String seriesName, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception;
	public List<SearchResult> findEpisode( String seriesName, Language audioLanguage, int absoluteEpisodeNumber) throws Exception;

}
