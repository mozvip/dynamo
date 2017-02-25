package com.github.dynamo.backlog.tasks.music;

import java.nio.file.Path;

import com.github.dynamo.backlog.tasks.files.ScanFolderTask;

public class ScanMusicFolderTask extends ScanFolderTask {
	
	public ScanMusicFolderTask( Path sourceFolder) {
		super( sourceFolder );
	}

	@Override
	public String toString() {
		return String.format("Scanning music from folder %s", getFolder().toAbsolutePath().toString());
	}

}
