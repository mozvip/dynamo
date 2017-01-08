package com.github.dynamo.backlog.tasks.tvshows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.finders.core.EpisodeFinder;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.FinderManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.backlog.find.FindEpisodeTask;
import com.github.dynamo.model.backlog.find.FindSeasonTask;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.TVShowManager;

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
	
	
	@Override
	public void cancel() {
		super.cancel();
		DownloadableManager.getInstance().logStatusChange( getDownloadable(), DownloadableStatus.IGNORED );
	}
	

}
