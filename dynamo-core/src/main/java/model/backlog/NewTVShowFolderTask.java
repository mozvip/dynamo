package model.backlog;

import java.nio.file.Path;

import dynamo.model.backlog.core.NewFolderTask;

public class NewTVShowFolderTask extends NewFolderTask {

	public NewTVShowFolderTask(Path path) {
		super( path );
	}

	@Override
	public String toString() {
		return String.format( "Adding new TV Show folder : %s", getFolder() );
	}

}
