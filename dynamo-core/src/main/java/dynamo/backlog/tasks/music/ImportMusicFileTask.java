package dynamo.backlog.tasks.music;

import java.nio.file.Path;

import dynamo.core.model.Task;

public class ImportMusicFileTask extends Task {

	private Path path;
	private boolean keepSourceFile;
	
	public ImportMusicFileTask( Path musicFilePath, boolean keepSourceFile ) {
		this.path = musicFilePath;
		this.keepSourceFile = keepSourceFile;
	}

	public Path getPath() {
		return path;
	}
	
	public boolean isKeepSourceFile() {
		return keepSourceFile;
	}
	
	@Override
	public String toString() {
		return String.format("Importing music file : %s", path.getFileName().toString());
	}

}
