package dynamo.manager.games;

import java.nio.file.Path;

import dynamo.games.model.GamePlatform;
import dynamo.model.backlog.core.NewFolderTask;

public class ScanGamesFolderTask extends NewFolderTask {
	
	private GamePlatform platform;

	public ScanGamesFolderTask(GamePlatform platform, Path path) {
		super(path);
		this.platform = platform;
	}
	
	public GamePlatform getPlatform() {
		return platform;
	}
	
	@Override
	public String toString() {
		return String.format( "Scanning folder %s for games platform %s", getFolder(), platform.getLabel() );
	}	

}
