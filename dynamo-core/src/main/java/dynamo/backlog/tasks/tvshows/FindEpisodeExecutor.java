package dynamo.backlog.tasks.tvshows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.FindDownloadableExecutor;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.EpisodeFinder;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.FinderManager;
import dynamo.model.backlog.find.FindEpisodeTask;
import dynamo.model.backlog.find.FindSeasonTask;
import dynamo.model.result.SearchResult;
import dynamo.model.tvshows.TVShowManager;
import model.ManagedEpisode;
import model.ManagedSeries;

public class FindEpisodeExecutor extends FindDownloadableExecutor<ManagedEpisode> {
	
	private ManagedEpisode episode;
	private ManagedSeries series;
	
	public FindEpisodeExecutor( FindEpisodeTask item, SearchResultDAO searchResultDAO ) {
		super(item, searchResultDAO);
		episode = item.getDownloadable();
		series = TVShowManager.getInstance().getManagedSeries( episode.getSeriesId() );
	}

	@Override
	public Collection<String> getWordsBlackList( ManagedEpisode downloadable ) {
		List<String> blackList = new ArrayList<>( TVShowManager.getInstance().getWordsBlackList() );
		if (series.getWordsBlackList() != null) {
			blackList.addAll( series.getWordsBlackList() );
		}
		return blackList;
	}

	@Override
	public List<?> getProviders() {
		return TVShowManager.getInstance().getTvshowEpisodeProviders();
	}

	@Override
	public List<SearchResult> getResults(DownloadFinder provider, ManagedEpisode episode) {
		EpisodeFinder episodeFinder = (EpisodeFinder) provider;
		
		Language audioLanguage = series.getAudioLanguage();
		
		List<SearchResult> allResults = new ArrayList<>();
		for (String aka : series.getAka()) {
			try {
				List<SearchResult> results = null;
				if (series.isUseAbsoluteNumbering()) {
					results = episodeFinder.findDownloadsForEpisode( aka, series, episode.getAbsoluteNumber() );
				} else {
					results = episodeFinder.findDownloadsForEpisode( aka, series, episode.getSeasonNumber(), episode.getEpisodeNumber() );	
				}
				if ( results != null ) {
					allResults.addAll( results );
				}
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
		
		return allResults;
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
