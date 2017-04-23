package com.github.dynamo.backlog.tasks.tvshows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.finders.core.TVShowSeasonProvider;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.FinderManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.backlog.core.FindDownloadableTask;
import com.github.dynamo.model.backlog.find.FindEpisodeTask;
import com.github.dynamo.model.backlog.find.FindSeasonTask;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.github.dynamo.tvshows.model.TVShowSeason;

public class FindSeasonExecutor extends AbstractFindTVShowExecutor<TVShowSeason> {

	
	private TVShowSeason season;

	public FindSeasonExecutor(FindSeasonTask task, SearchResultDAO searchResultDAO) {
		super(task, TVShowManager.getInstance().getManagedSeries( ((TVShowSeason) task.getDownloadable()).getSeriesId() ), searchResultDAO );
		season = (TVShowSeason) task.getDownloadable();
	}

	@Override
	public Collection<String> getWordsBlackList(TVShowSeason season) {
		Collection<String> wordsBlackList = TVShowManager.getInstance().getWordsBlackList();
		List<String> blackList = new ArrayList<>();
		if (wordsBlackList != null) {
			blackList.addAll( wordsBlackList );
		}
		if (series.getWordsBlackList() != null) {
			blackList.addAll( series.getWordsBlackList() );
		}
		return blackList;
	}

	@Override
	public List<?> getProviders() {
		return TVShowManager.getInstance().getTvShowSeasonProviders();
	}
	
	@Override
	public List<SearchResult> findForSearchString(DownloadFinder provider, String searchString, Language audioLanguage) throws Exception {
		return ((TVShowSeasonProvider)provider).findDownloadsForSeason( searchString, audioLanguage, season.getSeason() );
	}

	@Override
	public void rescheduleTask(FindDownloadableTask<TVShowSeason> task) {
		List<ManagedEpisode> episodes = TVShowManager.getInstance().findEpisodesForSeason( task.getDownloadable().getId() );
		if ( mustReschedule ) {
			// switch to episode search in this case
			for (ManagedEpisode episode : episodes) {
				DownloadableManager.getInstance().want( episode );
			}
		} else {
			// search was successfull
			for (ManagedEpisode episode : episodes) {
				DownloadableManager.getInstance().snatched( episode, selectedResult );
				// cancel search for individual episode
				BackLogProcessor.getInstance().unschedule( FindEpisodeTask.class, "task.downloadable.id == " + episode.getId() );
			}
		}
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

