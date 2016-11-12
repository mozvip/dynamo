package dynamo.manager.games;

import java.nio.file.Path;

import dynamo.backlog.tasks.files.ScanFolderTask;
import dynamo.games.model.GamePlatform;

public class ScanGamesFolderTask extends ScanFolderTask {
	
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
