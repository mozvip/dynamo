package dynamo.backlog.tasks.tvshows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.finders.core.EpisodeFinder;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.FinderManager;
import dynamo.model.backlog.find.FindEpisodeTask;
import dynamo.model.backlog.find.FindSeasonTask;
import dynamo.model.result.SearchResult;
import dynamo.model.tvshows.TVShowManager;
import model.ManagedEpisode;

public class FindEpisodeExecutor extends AbstractFindTVShowExecutor<ManagedEpisode> {
	
	private ManagedEpisode episode;
	
	public FindEpisodeExecutor( FindEpisodeTask task, SearchResultDAO searchResultDAO ) {
		super(task, TVShowManager.getInstance().getManagedSeries( task.getDownloadable().getSeriesId() ), searchResultDAO);
		episode = task.getDownloadable();
	}

	@Override
	public List<?> getProviders() {
		return TVShowManager.getInstance().getTvshowEpisodeProviders();
	}
	
	public List<SearchResult> filter( List<SearchResult> unfilteredResults ) {
		
		List<SearchResult> results;
		if (series.getQualities() != null) {
			Set<String> aliases = new HashSet<>();
			for (VideoQuality quality : series.getQualities()) {
				aliases.addAll( Arrays.asList( quality.getAliases()) );
			}
			String[] qualities = (String[]) aliases.toArray(new String[aliases.size()]);
			results = new ArrayList<>();
			results.addAll( unfilteredResults.stream().filter( tvshow -> StringUtils.containsAny(tvshow.getTitle(), qualities)).collect( Collectors.toList()) );
		} else {
			results = unfilteredResults;
		}
		
		return results;
	}

	@Override
	public List<SearchResult> findForSearchString(DownloadFinder provider, String searchString, Language audioLanguage) throws Exception {
		EpisodeFinder episodeFinder = (EpisodeFinder) provider;
		List<SearchResult> results = null;
		if (series.isUseAbsoluteNumbering()) {
			results = episodeFinder.findEpisode( searchString, audioLanguage, episode.getAbsoluteNumber() );
		} else {
			results = episodeFinder.findEpisode( searchString, audioLanguage, episode.getSeasonNumber(), episode.getEpisodeNumber() );
		}
		return filter( results );
	}
	
	@Override
	protected void selectResult(SearchResult selectedResult) {
		super.selectResult(selectedResult);
		BackLogProcessor.getInstance().unschedule( FindSeasonTask.class, String.format("this.downloadable.id == %d", episode.getSeasonId() ));
	}
	
	@Override
	public int evaluateResult(SearchResult result) {
		return FinderManager.getInstance().evaluateResultForSeries( series, result );
	}	

}
