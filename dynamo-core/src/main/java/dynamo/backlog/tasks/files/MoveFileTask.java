package dynamo.backlog.tasks.files;

import java.nio.file.Path;

import dynamo.model.Downloadable;

public class MoveFileTask extends FileOperationTask {
	
	private Path source;
	private Path destination;

	public MoveFileTask(Path source, Path destination, Downloadable downloadable) {
		super( downloadable );
		this.source = source;
		this.destination = destination;
		this.downloadable = downloadable;
	}

	public Path getSource() {
		return source;
	}
	public void setSource(Path source) {
		this.source = source;
	}
	public Path getDestination() {
		return destination;
	}
	public void setDestination(Path destination) {
		this.destination = destination;
	}

	@Override
	public String toString() {
		return String.format("Moving file from %s to %s for <a href='%s'>%s</a>", source.toAbsolutePath().toString(), destination.toAbsolutePath().toString(), downloadable.getRelativeLink(), downloadable);
	}

}
