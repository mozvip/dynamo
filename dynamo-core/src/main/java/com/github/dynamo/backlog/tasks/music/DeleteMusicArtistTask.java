package com.github.dynamo.backlog.tasks.music;

import com.github.dynamo.core.model.Task;

public class DeleteMusicArtistTask extends Task {
	
	private String artistName;
	
	public DeleteMusicArtistTask( String artistName ) {
		this.artistName	= artistName;
	}

	public String getArtistName() {
		return artistName;
	}
	
	@Override
	public String toString() {
		return String.format("Deleting music artist : %s", artistName);
	}
	
}
