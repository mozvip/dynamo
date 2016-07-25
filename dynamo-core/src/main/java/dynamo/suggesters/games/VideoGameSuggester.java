package dynamo.suggesters.games;

import java.util.List;

import dynamo.games.model.GamePlatform;
import dynamo.games.model.VideoGame;

public interface VideoGameSuggester {

	public GamePlatform[] getSupportedPlatforms();

	public List<VideoGame> suggestGames( GamePlatform platform );

}
