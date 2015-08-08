package dynamo.suggesters.games;

import java.util.List;

import dynamo.model.games.GamePlatform;
import dynamo.model.games.VideoGame;

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
