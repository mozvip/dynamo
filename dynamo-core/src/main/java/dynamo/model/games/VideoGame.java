package dynamo.model.games;

import java.nio.file.Path;

import core.FileNameUtils;
import dynamo.manager.games.GamesManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;

public class VideoGame extends Downloadable {

	private GamePlatform platform;
	private Long theGamesDbId;

	public VideoGame( Long id, DownloadableStatus status, String coverImage, String name, GamePlatform platform, Long theGamesDbId) {
		super( id, name, null, status, coverImage, null,-1,  null);
		this.platform = platform;
		this.theGamesDbId = theGamesDbId;
	}

	public GamePlatform getPlatform() {
		return platform;
	}
	
	public Long getTheGamesDbId() {
		return theGamesDbId;
	}

	@Override
	public String getRelativeLink() {
		if (isDownloaded()) {
			return String.format("games-collection.jsf?platform=%s", platform.name());
		} else {
			return String.format("games-wanted.jsf?platform=%s", platform.name());
		}
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", getName(), platform.name());
	}

	@Override
	public Path determineDestinationFolder() {
		return GamesManager.getInstance().getFolder( getPlatform() ).resolve( FileNameUtils.sanitizeFileName( getName() ) );
	}

}
