package dynamo.backlog.tasks.tvshows;

import java.util.List;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
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

	@Override
	public List<SearchResult> findForSearchString(DownloadFinder provider, String searchString, Language audioLanguage) throws Exception {
		EpisodeFinder episodeFinder = (EpisodeFinder) provider;
		if (series.isUseAbsoluteNumbering()) {
			return episodeFinder.findDownloadsForEpisode( searchString, audioLanguage, episode.getAbsoluteNumber() );
		} else {
			return episodeFinder.findDownloadsForEpisode( searchString, audioLanguage, episode.getSeasonNumber(), episode.getEpisodeNumber() );
		}
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
