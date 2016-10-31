package dynamo.games;

import java.util.List;

import dynamo.games.model.GamePlatform;
import dynamo.games.model.VideoGame;

public interface GameSuggester {

	public List<VideoGame> suggestGames( GamePlatform platform );

}
