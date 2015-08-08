package dynamo.providers;

import java.util.List;

import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.finders.core.EpisodeFinder;
import dynamo.model.result.SearchResult;

public class BinNewzFranceProvider extends DownloadFinder implements EpisodeFinder {

	public BinNewzFranceProvider() {
		super("http://www.binnews.in/");
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode(String seriesName,
			Language audioLanguage, int seasonNumber, int episodeNumber)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel() {
		return "BinnewZ France";
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode(String seriesName,
			Language audioLanguage, int absoluteEpisodeNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configureProvider() {
	}

}
