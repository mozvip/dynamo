package dynamo.model.backlog.core;

import java.nio.file.Path;

import dynamo.backlog.tasks.tvshows.ScanFileSystemQueue;
import dynamo.core.DynamoTask;
import dynamo.core.model.Task;

@DynamoTask(queueClass=ScanFileSystemQueue.class)
public abstract class NewFolderTask extends Task {
	
	private Path folder;
	
	public NewFolderTask(Path path) {
		this.folder = path.toAbsolutePath().normalize();
	}

	public Path getFolder() {
		return folder;
	}
	
	public void setFolder(Path folder) {
		this.folder = folder;
	}

}
