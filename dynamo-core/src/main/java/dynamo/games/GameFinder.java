package dynamo.games;

import java.util.List;

import dynamo.games.model.VideoGame;
import dynamo.model.result.SearchResult;

public interface GameFinder {
	
	public List<SearchResult> findGame( VideoGame videoGame ) throws Exception;

}
