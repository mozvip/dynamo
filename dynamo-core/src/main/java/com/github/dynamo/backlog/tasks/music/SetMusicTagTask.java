package com.github.dynamo.backlog.tasks.music;


import com.github.dynamo.core.model.Task;
import com.github.dynamo.model.music.MusicFile;

public class SetMusicTagTask extends Task {

	private MusicFile musicFile;

	private String album;
	private String songArtist;
	private String albumArtist;

	public SetMusicTagTask( MusicFile file, String songArtist, String albumArtist, String album) {
		super();
		this.musicFile = file;
		this.songArtist = songArtist;
		this.album = album;
		this.albumArtist = albumArtist;
	}

	public MusicFile getMusicFile() {
		return musicFile;
	}

	public String getSongArtist() {
		return songArtist;
	}

	public void setSongArtist(String songArtist) {
		this.songArtist = songArtist;
	}

	public String getAlbumArtist() {
		return albumArtist;
	}
	
	public void setAlbumArtist(String albumArtist) {
		this.albumArtist = albumArtist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	@Override
	public String toString() {
		return String.format( "Updating music tags for %s", musicFile );
	}

}
