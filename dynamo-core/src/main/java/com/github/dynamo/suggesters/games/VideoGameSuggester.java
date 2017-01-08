package com.github.dynamo.suggesters.games;

import java.util.List;

import com.github.dynamo.games.model.GamePlatform;
import com.github.dynamo.games.model.VideoGame;

public interface VideoGameSuggester {

	public GamePlatform[] getSupportedPlatforms();

	public List<VideoGame> suggestGames( GamePlatform platform );

}
