package com.github.dynamo.model.backlog.find;

import com.github.dynamo.model.backlog.core.FindDownloadableTask;
import com.github.dynamo.model.music.MusicAlbum;

public class FindMusicAlbumTask extends FindDownloadableTask<MusicAlbum> {
	
	public FindMusicAlbumTask(MusicAlbum musicAlbum) {
		super( musicAlbum );
	}

	@Override
	public String toString() {
		return String.format("Find album : %s", downloadable.toString()); 
	}

}
