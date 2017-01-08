package com.github.dynamo.games;

import java.util.List;

import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.model.result.SearchResult;

public interface GameFinder {
	
	public List<SearchResult> findGame( VideoGame videoGame ) throws Exception;

}
