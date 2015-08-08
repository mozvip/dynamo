package dynamo.model.backlog.core;

import java.nio.file.Path;

import dynamo.core.model.Task;

public class PostProcessFolderTask extends Task {
	
	public PostProcessFolderTask( Path folder ) {
		this.folder = folder;
	}
	
	private Path folder;
	
	public Path getFolder() {
		return folder;
	}
	
	public void setFolder(Path folder) {
		this.folder = folder;
	}
	
	@Override
	public String toString() {
		return String.format( "Post Processing folder %s", getFolder().toAbsolutePath().toString() ); 
	}

}
