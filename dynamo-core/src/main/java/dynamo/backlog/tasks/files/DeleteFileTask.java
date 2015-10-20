package dynamo.backlog.tasks.files;

import java.nio.file.Path;

import dynamo.core.DynamoTask;
import dynamo.core.model.Task;

@DynamoTask
public class DeleteFileTask extends Task {
	
	private Path path;

	public DeleteFileTask(Path path) {
		super();
		this.path = path;
	}
	
	public Path getPath() {
		return path;
	}

}
