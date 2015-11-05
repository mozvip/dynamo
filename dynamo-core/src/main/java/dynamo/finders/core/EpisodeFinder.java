package dynamo.finders.core;

import java.util.List;

import dynamo.core.Language;
import dynamo.model.result.SearchResult;
import model.ManagedSeries;

public interface EpisodeFinder {
	
	public List<SearchResult> findDownloadsForEpisode( String seriesName, ManagedSeries series, int seasonNumber, int episodeNumber) throws Exception;
	public List<SearchResult> findDownloadsForEpisode( String seriesName, ManagedSeries series, int absoluteEpisodeNumber) throws Exception;

}
