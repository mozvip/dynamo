package com.github.dynamo.suggesters.games;

import java.util.List;

import com.github.dynamo.games.model.GamePlatform;
import com.github.dynamo.games.model.VideoGame;

public class PreDBVideoGameSuggester implements VideoGameSuggester {

	@Override
	public GamePlatform[] getSupportedPlatforms() {
		return new GamePlatform[] { GamePlatform.PC, GamePlatform.XBOX360, GamePlatform.PS3, GamePlatform.NINTENDO_DS, GamePlatform.NINTENDO_WII };
	}

	@Override
	public List<VideoGame> suggestGames(GamePlatform platform) {
		// TODO Auto-generated method stub
		return null;
	}

}
