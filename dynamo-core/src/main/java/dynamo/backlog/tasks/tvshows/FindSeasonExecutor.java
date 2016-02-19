package dynamo.backlog.tasks.tvshows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.finders.core.TVShowSeasonProvider;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.FinderManager;
import dynamo.model.backlog.core.FindDownloadableTask;
import dynamo.model.backlog.find.FindEpisodeTask;
import dynamo.model.backlog.find.FindSeasonTask;
import dynamo.model.result.SearchResult;
import dynamo.model.tvshows.TVShowManager;
import dynamo.model.tvshows.TVShowSeason;
import model.ManagedEpisode;

public class FindSeasonExecutor extends AbstractFindTVShowExecutor<TVShowSeason> {

	
	private TVShowSeason season;

	public FindSeasonExecutor(FindSeasonTask task, SearchResultDAO searchResultDAO) {
		super(task, TVShowManager.getInstance().getManagedSeries( ((TVShowSeason) task.getDownloadable()).getSeriesId() ), searchResultDAO );
		season = (TVShowSeason) task.getDownloadable();
	}

	@Override
	public Collection<String> getWordsBlackList(TVShowSeason season) {
		List<String> blackList = new ArrayList<>( TVShowManager.getInstance().getWordsBlackList() );
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
				BackLogProcessor.getInstance().unschedule( FindEpisodeTask.class, "this.downloadable.id == " + episode.getId() );
			}
		}
	}

	@Override
	public int evaluateResult(SearchResult result) {
		return FinderManager.getInstance().evaluateResultForSeries( series, result );
	}

}

