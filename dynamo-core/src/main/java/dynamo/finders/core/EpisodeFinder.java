package dynamo.finders.core;

import java.util.List;

import dynamo.core.Language;
import dynamo.model.result.SearchResult;

public interface EpisodeFinder {
	
	public List<SearchResult> findEpisode( String seriesName, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception;
	public List<SearchResult> findEpisode( String seriesName, Language audioLanguage, int absoluteEpisodeNumber) throws Exception;

}
