package dynamo.backlog.tasks.music;

import java.nio.file.Path;

import dynamo.core.model.Task;

public class SynchronizeMusicTagsTask extends Task {
	
	private Path musicFilePath;
	
	public SynchronizeMusicTagsTask(Path musicFilePath) {
		super();
		this.musicFilePath = musicFilePath;
	}
	
	public Path getMusicFilePath() {
		return musicFilePath;
	}

	@Override
	public String toString() {
		return String.format( "Synchronize music tags for %s", musicFilePath.toString() );
	}

}
