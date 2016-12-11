package dynamo.backlog.tasks.files;

import java.nio.file.Path;

import dynamo.backlog.tasks.core.ImmediateTask;
import dynamo.core.model.Task;

public class DeleteFileTask extends Task implements ImmediateTask {
	
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
