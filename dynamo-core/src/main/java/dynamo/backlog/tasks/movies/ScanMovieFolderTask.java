package dynamo.backlog.tasks.movies;

import java.nio.file.Path;

import dynamo.backlog.tasks.files.ScanFolderTask;

public class ScanMovieFolderTask extends ScanFolderTask {

	public ScanMovieFolderTask(Path path) {
		super( path );
	}

	@Override
	public String toString() {
		return String.format( "Adding new movie folder : %s", getFolder() );
	}
	

}

