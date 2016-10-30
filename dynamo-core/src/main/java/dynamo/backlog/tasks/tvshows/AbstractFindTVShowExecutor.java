package dynamo.backlog.tasks.tvshows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dynamo.backlog.tasks.core.FindDownloadableExecutor;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.jdbi.SearchResultDAO;
import dynamo.model.Downloadable;
import dynamo.model.backlog.core.FindDownloadableTask;
import dynamo.model.result.SearchResult;
import dynamo.tvshows.model.ManagedSeries;
import dynamo.tvshows.model.TVShowManager;

public abstract class AbstractFindTVShowExecutor<T extends Downloadable> extends FindDownloadableExecutor<T> {
	
	protected ManagedSeries series = null;
	
	public AbstractFindTVShowExecutor(FindDownloadableTask<T> task, ManagedSeries series, SearchResultDAO searchResultDAO) {
		super(task, searchResultDAO);
		this.series = series;
	}
	
	@Override
	public Collection<String> getWordsBlackList( T downloadable ) {
		List<String> blackList = new ArrayList<>();

		Collection<String> wordsBlackList = TVShowManager.getInstance().getWordsBlackList();
		if (wordsBlackList != null) {
			blackList.addAll( wordsBlackList );
		}

		if (series.getWordsBlackList() != null) {
			blackList.addAll( series.getWordsBlackList() );
		}
		return blackList;
	}
	
	@Override
	public List<SearchResult> getResults(DownloadFinder provider, T downloadable) {
		List<SearchResult> allResults = new ArrayList<>();
		for (String aka : series.getAka()) {
			try {
				List<String> searchStrings = new ArrayList<>();
				if (provider.needsLanguageInSearchString()) {
					if (series.getOriginalLanguage() != null && series.getAudioLanguage() != null && series.getAudioLanguage() != series.getOriginalLanguage()) {
						for (String lang : series.getAudioLanguage().getFullNames()) {
							searchStrings.add( aka + " " + lang );
						}
					}
				}

				if (searchStrings.isEmpty()) {
					searchStrings.add( aka );
				}

				for (String searchString : searchStrings) {
					try {
						allResults.addAll( findForSearchString( provider, searchString, series.getAudioLanguage() ) );
					} catch (Exception e) {
						ErrorManager.getInstance().reportThrowable(e);
					}
				}
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}

		return allResults;
	}
	
	public abstract List<SearchResult> findForSearchString( DownloadFinder provider, String searchString, Language audioLanguage ) throws Exception;

}
