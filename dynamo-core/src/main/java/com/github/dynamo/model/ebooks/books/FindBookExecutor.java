package com.github.dynamo.model.ebooks.books;

import java.util.Collection;
import java.util.List;

import com.github.dynamo.backlog.tasks.core.FindDownloadableExecutor;
import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.model.result.SearchResult;

public class FindBookExecutor extends FindDownloadableExecutor<Book> {

	public FindBookExecutor(FindBookTask task, SearchResultDAO searchResultDAO) {
		super(task, searchResultDAO);
	}

	@Override
	public Collection<String> getWordsBlackList(Book downloadable) {
		return BookManager.getInstance().getBlackList();
	}

	@Override
	public List<BookFinder> getProviders() {
		return BookManager.getInstance().getProviders();
	}

	@Override
	public List<SearchResult> getResults(DownloadFinder finder, Book book) {
		BookFinder bf = (BookFinder) finder;
		try {
			return bf.findBook( book );
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
		return null;
	}

	@Override
	public int evaluateResult(SearchResult result) {
		// TODO Auto-generated method stub
		return 0;
	}

}
