package com.github.dynamo.backlog.tasks.files;

import java.nio.file.Files;
import java.nio.file.Path;

import com.github.dynamo.backlog.tasks.core.ImmediateTask;
import com.github.dynamo.core.model.Task;

public class DeleteTask extends Task implements ImmediateTask {

	private Path path;
	private boolean removeParentFolderIfEmpty = false;

	public DeleteTask( Path path, boolean removeParentFolderIfEmpty ) {
		this.path = path;
		this.removeParentFolderIfEmpty = removeParentFolderIfEmpty;
	}

	public Path getPath() {
		return path;
	}
	
	public boolean isRemoveParentFolderIfEmpty() {
		return removeParentFolderIfEmpty;
	}

	@Override
	public String toString() {
		if (Files.isDirectory( path )) {
			return String.format( "Deleting folder %s", path.toAbsolutePath().toString() );
		} else {
			return String.format( "Deleting file %s", path.toAbsolutePath().toString() );
		}
	}

}
