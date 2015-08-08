package dynamo.backlog.tasks.tvshows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.tasks.core.FindDownloadableExecutor;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.SeasonFinder;
import dynamo.jdbi.SearchResultDAO;
import dynamo.jdbi.TVShowDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.FinderManager;
import dynamo.model.Downloadable;
import dynamo.model.backlog.core.FindDownloadableTask;
import dynamo.model.backlog.find.FindSeasonTask;
import dynamo.model.result.SearchResult;
import dynamo.model.tvshows.TVShowManager;
import dynamo.model.tvshows.TVShowSeason;
import model.ManagedEpisode;
import model.ManagedSeries;

public class FindSeasonExecutor extends FindDownloadableExecutor {
	
	private TVShowDAO tvShowDAO;
	
	private TVShowSeason season;
	private ManagedSeries series;

	public FindSeasonExecutor(FindSeasonTask item, SearchResultDAO searchResultDAO, TVShowDAO tvShowDAO) {
		super(item, searchResultDAO );
		this.tvShowDAO = tvShowDAO;
		season = (TVShowSeason) item.getDownloadable();
		series = TVShowManager.getInstance().getManagedSeries( season.getSeriesId() );
	}

	@Override
	public Collection<String> getWordsBlackList(Downloadable downloadable) {
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
	
	protected List<SearchResult> search( SeasonFinder seasonFinder, Language audioLanguage, int seasonNumber ) {
		List<SearchResult> allResults = new ArrayList<>();
		for (String aka : series.getAka()) {
			try {
				List<SearchResult> results = seasonFinder.findDownloadsForSeason( aka, audioLanguage, season.getSeason() );
				for (Iterator<SearchResult> iterator = results.iterator(); iterator.hasNext();) {
					SearchResult searchResult = iterator.next();
					if (!StringUtils.startsWithIgnoreCase(searchResult.getTitle(), aka)) {
						iterator.remove();
					}
				}
				allResults.addAll( results );
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
		return allResults;
	}

	@Override
	public List<SearchResult> getResults(DownloadFinder finder, Downloadable downloadable) {

		Language audioLanguage = series.getAudioLanguage();

		SeasonFinder seasonFinder = (SeasonFinder) finder;
		List<SearchResult> allResults = search( seasonFinder, audioLanguage, season.getSeason() );
		if (allResults.isEmpty() && series.isEnded()) {
			List<TVShowSeason> allSeasons = tvShowDAO.findSeasons( series.getId() );
			TVShowSeason lastSeason = allSeasons.get( allSeasons.size() - 1 );
			if (lastSeason.getSeason() == 1) {
				// only one season, try search with season = -1 to indicate we don't want to specify a season
				allResults = search( seasonFinder, audioLanguage, -1 );
			}
		}		
		return allResults;
	}
	
	@Override
	public void rescheduleTask(FindDownloadableTask item) {
		if ( mustReschedule ) {
			// switch to episode search in this case
			List<ManagedEpisode> episodes = tvShowDAO.findEpisodesForSeason( item.getDownloadable().getId() );
			for (ManagedEpisode episode : episodes) {
				DownloadableManager.getInstance().want( episode );
			}
		}
	}
	
	@Override
	public int evaluateResult(SearchResult result) {
		return FinderManager.getInstance().evaluateResultForSeries( series, result );
	}

}

