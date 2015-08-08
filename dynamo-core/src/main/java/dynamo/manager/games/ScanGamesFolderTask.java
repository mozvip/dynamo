package dynamo.manager.games;

import java.nio.file.Path;

import dynamo.model.backlog.core.NewFolderTask;
import dynamo.model.games.GamePlatform;

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
