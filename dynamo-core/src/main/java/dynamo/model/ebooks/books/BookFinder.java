package dynamo.model.ebooks.books;

import java.util.List;

import dynamo.model.result.SearchResult;

public interface BookFinder {
	
	public List<SearchResult> findBook( Book book ) throws Exception;

}
