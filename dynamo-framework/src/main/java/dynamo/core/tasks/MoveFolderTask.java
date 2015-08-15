package dynamo.core.tasks;

import java.nio.file.Path;

import dynamo.backlog.queues.DynamoFileOperationQueue;
import dynamo.core.DynamoTask;
import dynamo.core.model.Task;

@DynamoTask(queueClass=DynamoFileOperationQueue.class)
public class MoveFolderTask extends Task {
	
	Path sourceFolder;
	Path destinationFolder;
	
	public MoveFolderTask(Path sourceFolder, Path destinationFolder) {
		this.sourceFolder = sourceFolder;
		this.destinationFolder = destinationFolder;
	}

	public Path getSourceFolder() {
		return sourceFolder;
	}

	public void setSourceFolder(Path sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	public Path getDestinationFolder() {
		return destinationFolder;
	}

	public void setDestinationFolder(Path destinationFolder) {
		this.destinationFolder = destinationFolder;
	}
	
	@Override
	public String toString() {
		return String.format("Moving folder %s to %s", sourceFolder.toAbsolutePath().toString(), destinationFolder.toAbsolutePath().toString() );
	}

}
