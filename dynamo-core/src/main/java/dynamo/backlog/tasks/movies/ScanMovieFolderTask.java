package dynamo.backlog.tasks.movies;

import java.nio.file.Path;

import dynamo.model.backlog.core.NewFolderTask;

public class ScanMovieFolderTask extends NewFolderTask {

	public ScanMovieFolderTask(Path path) {
		super( path );
	}

	@Override
	public String toString() {
		return String.format( "Adding new movie folder : %s", getFolder() );
	}
	

}

