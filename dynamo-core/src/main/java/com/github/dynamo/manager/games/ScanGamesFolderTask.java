package com.github.dynamo.manager.games;

import java.nio.file.Path;

import com.github.dynamo.backlog.tasks.files.ScanFolderTask;
import com.github.dynamo.games.model.GamePlatform;

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
