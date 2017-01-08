package com.github.dynamo.backlog.tasks.music;

import java.nio.file.Path;

import com.github.dynamo.core.model.Task;

public class CalcAcoustIdTask extends Task {

	private Path musicFilePath;
	
	public CalcAcoustIdTask( Path path ) {
		this.musicFilePath = path;
	}
	
	public Path getMusicFilePath() {
		return musicFilePath;
	}
	
	@Override
	public String toString() {
		return String.format( "Calculating AcoustID for %s", musicFilePath.toString() );
	}

}
