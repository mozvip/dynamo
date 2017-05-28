package com.github.dynamo.backlog.tasks.tvshows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.backlog.tasks.core.FindDownloadableExecutor;
import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.backlog.core.FindDownloadableTask;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.tvshows.model.ManagedSeries;
import com.github.dynamo.tvshows.model.TVShowManager;

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
		for (String aka : series.getAllNames()) {
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
						searchString = StringUtils.stripAccents( searchString );
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
