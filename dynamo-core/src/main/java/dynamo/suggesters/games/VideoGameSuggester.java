package dynamo.suggesters.games;

import java.util.List;

import dynamo.model.games.GamePlatform;
import dynamo.model.games.VideoGame;

public interface VideoGameSuggester {

	public GamePlatform[] getSupportedPlatforms();

	public List<VideoGame> suggestGames( GamePlatform platform );

}
