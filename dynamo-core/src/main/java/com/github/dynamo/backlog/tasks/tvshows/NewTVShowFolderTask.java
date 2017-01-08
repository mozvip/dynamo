package com.github.dynamo.backlog.tasks.tvshows;

import java.nio.file.Path;

import com.github.dynamo.backlog.tasks.files.ScanFolderTask;

public class NewTVShowFolderTask extends ScanFolderTask {

	public NewTVShowFolderTask(Path path) {
		super( path );
	}

	@Override
	public String toString() {
		return String.format( "Adding new TV Show folder : %s", getFolder() );
	}

}
