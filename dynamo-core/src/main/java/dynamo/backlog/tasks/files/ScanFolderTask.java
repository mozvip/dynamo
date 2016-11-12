package dynamo.backlog.tasks.files;

import java.nio.file.Path;

import dynamo.core.model.Task;

public abstract class ScanFolderTask extends Task {
	
	private Path folder;

	public ScanFolderTask( Path folder ) {
		this.folder = folder.toAbsolutePath().normalize();
	}
	
	public Path getFolder() {
		return folder;
	}
	
	@Override
	public String toString() {
		return String.format( "Scan folder %s", folder.toString() );
	}	
	
}
