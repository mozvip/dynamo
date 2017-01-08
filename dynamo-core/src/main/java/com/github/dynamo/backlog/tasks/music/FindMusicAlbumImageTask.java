package com.github.dynamo.backlog.tasks.music;

import com.github.dynamo.backlog.tasks.core.FindDownloadableImageTask;
import com.github.dynamo.model.music.MusicAlbum;

public class FindMusicAlbumImageTask extends FindDownloadableImageTask<MusicAlbum> {

	public FindMusicAlbumImageTask( MusicAlbum album ) {
		super(album);
	}
	
	@Override
	public String toString() {
		return String.format( "Finding image for album <a href='%s'>%s</a>", getDownloadable().getRelativeLink(), getDownloadable().toString());
	}

}
