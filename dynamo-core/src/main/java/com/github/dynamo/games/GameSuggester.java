package com.github.dynamo.games;

import java.util.List;

import com.github.dynamo.games.model.GamePlatform;
import com.github.dynamo.games.model.VideoGame;

public interface GameSuggester {

	public List<VideoGame> suggestGames( GamePlatform platform );

}
