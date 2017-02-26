package com.github.dynamo.backlog.tasks.files;

import java.nio.file.Path;

public class DeleteFileEvent {
	
	private Path path;

	public DeleteFileEvent(Path path) {
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
