package com.github.dynamo.backlog.tasks.music;

import com.github.dynamo.core.model.Task;
import com.github.dynamo.model.music.MusicArtist;

public class MusicArtistToggleFavoriteTask extends Task {

	private MusicArtist artist;
	private boolean favorite;

	public MusicArtistToggleFavoriteTask(MusicArtist artist, boolean favorite) {
		super();
		this.artist = artist;
		this.favorite = favorite;
	}

	public MusicArtist getArtist() {
		return artist;
	}
	
	public boolean isFavorite() {
		return favorite;
	}
	
	@Override
	public String toString() {
		if (isFavorite()) {
			return String.format("Set %s as favorite", artist.getName());
		} else {
			return String.format("Unfavorite %s", artist.getName());
		}
	}

}
