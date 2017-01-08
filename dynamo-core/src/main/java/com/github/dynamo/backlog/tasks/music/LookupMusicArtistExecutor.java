package com.github.dynamo.backlog.tasks.music;

import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.model.music.MusicArtist;
import com.github.dynamo.music.TheAudioDb;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;

public class LookupMusicArtistExecutor extends TaskExecutor<LookupMusicArtistTask> {
	
	private MusicAlbumDAO musicDAO;
	private DownloadableUtilsDAO downloadableDAO;
	
	public LookupMusicArtistExecutor(LookupMusicArtistTask item, MusicAlbumDAO musicDAO, DownloadableUtilsDAO downloadableDAO) {
		super(item);
		this.musicDAO = musicDAO;
		this.downloadableDAO = downloadableDAO;
	}

	@Override
	public void execute() throws Exception {
		
		MusicArtist artist = task.getArtist();
		
		// FIXME

		TheAudioDb.getInstance().searchAlbums( artist.getName() );
		
	}

}
