package dynamo.finders.core;

import java.util.List;

import dynamo.model.games.VideoGame;
import dynamo.model.result.SearchResult;

public interface GameFinder {
	
	public List<SearchResult> findGame( VideoGame videoGame ) throws Exception;

}
