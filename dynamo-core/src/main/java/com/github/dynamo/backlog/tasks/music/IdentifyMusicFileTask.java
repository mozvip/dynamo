package com.github.dynamo.backlog.tasks.music;

import java.nio.file.Path;

import com.github.dynamo.core.model.Task;

public class IdentifyMusicFileTask extends Task {
	
	private Path musicFilePath;
	
	public IdentifyMusicFileTask( Path musicFilePath ) {
		super();
		this.musicFilePath = musicFilePath;
	}

	public Path getMusicFilePath() {
		return musicFilePath;
	}

	@Override
	public String toString() {
		return String.format("Identify music file : %s", musicFilePath.toString());
	}

}
