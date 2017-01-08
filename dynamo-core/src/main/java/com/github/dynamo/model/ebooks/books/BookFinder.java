package com.github.dynamo.model.ebooks.books;

import java.util.List;

import com.github.dynamo.model.result.SearchResult;

public interface BookFinder {
	
	public List<SearchResult> findBook( Book book ) throws Exception;

}
