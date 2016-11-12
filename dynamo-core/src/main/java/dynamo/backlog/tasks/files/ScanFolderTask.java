package dynamo.backlog.tasks.files;

import java.nio.file.Path;

import dynamo.backlog.tasks.tvshows.FileSystemQueue;
import dynamo.core.DynamoTask;
import dynamo.core.model.Task;

@DynamoTask(queueClass=FileSystemQueue.class)
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
