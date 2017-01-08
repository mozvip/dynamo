package com.github.dynamo.backlog.tasks.music;

import java.nio.file.Path;

import com.github.dynamo.core.model.Task;
import com.github.dynamo.model.music.MusicAlbum;

public class SetAlbumImageTask extends Task {
	
	private MusicAlbum musicAlbum;
	private Path localImagePath;

	public SetAlbumImageTask(MusicAlbum musicAlbum, Path localImagePath) {
		super();
		this.musicAlbum = musicAlbum;
		this.localImagePath = localImagePath;
	}
	
	public Path getLocalImagePath() {
		return localImagePath;
	}
	
	public MusicAlbum getMusicAlbum() {
		return musicAlbum;
	}

	@Override
	public String toString() {
		return String.format("Set album image for %s", musicAlbum.toString());
	}

}
