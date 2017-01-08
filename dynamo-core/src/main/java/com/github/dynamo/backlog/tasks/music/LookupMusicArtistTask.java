package com.github.dynamo.backlog.tasks.music;

import com.github.dynamo.core.model.Task;
import com.github.dynamo.model.music.MusicArtist;

public class LookupMusicArtistTask extends Task {

	private MusicArtist artist;
	
	public LookupMusicArtistTask( MusicArtist artist ) {
		this.artist = artist;
	}
	
	public MusicArtist getArtist() {
		return artist;
	}
	
	public void setArtist(MusicArtist artist) {
		this.artist = artist;
	}
	
	@Override
	public String toString() {
		return String.format("Look up artist metadata for %s", artist.getName());
	}

}
