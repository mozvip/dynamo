package dynamo.model.games;

import java.nio.file.Path;

import core.FileNameUtils;
import dynamo.manager.games.GamesManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;

public class VideoGame extends Downloadable {

	private String name;
	private GamePlatform platform;
	private Long theGamesDbId;

	public VideoGame( Long id, DownloadableStatus status, Path path, String coverImage, String name, GamePlatform platform, Long theGamesDbId) {
		super( id, status, path, coverImage, null, null);
		this.name = name;
		this.platform = platform;
		this.theGamesDbId = theGamesDbId;
	}

	public String getName() {
		return name;
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
		return String.format("%s (%s)", name, platform.name());
	}

	@Override
	public Path getDestinationFolder() {
		return GamesManager.getInstance().getFolder( getPlatform() ).resolve( FileNameUtils.sanitizeFileName( getName() ) );
	}

}
