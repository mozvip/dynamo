package dynamo.backlog.tasks.files;

import java.nio.file.Path;

import dynamo.core.DynamoTask;
import dynamo.core.model.Task;

@DynamoTask(queueClass=DeleteQueue.class)
public class DeleteFileTask extends Task {
	
	private Path path;

	public DeleteFileTask(Path path) {
		super();
		this.path = path;
	}
	
	public Path getPath() {
		return path;
	}

	@Override
	public String toString() {
		return String.format( "Deleting %s", path.toAbsolutePath().toString() );
	}	
}
