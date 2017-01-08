package com.github.dynamo.backlog.tasks.music;

import java.nio.file.Path;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.model.music.MusicArtist;
import com.github.dynamo.model.music.MusicFile;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;

public class SetMusicTagTaskExecutor extends TaskExecutor<SetMusicTagTask> {
	
	private DownloadableUtilsDAO downloadableUtilsDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );
	private MusicAlbumDAO musicDAO = DAOManager.getInstance().getDAO( MusicAlbumDAO.class );

	public SetMusicTagTaskExecutor(SetMusicTagTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {

		MusicFile musicFile = task.getMusicFile();

		MusicAlbum musicAlbum = musicDAO.find( musicFile.getDownloadableId() );

		if ( task.getAlbumArtist() != null || task.getAlbum() != null ) {
			String albumArtist = task.getAlbumArtist() != null ? task.getAlbumArtist() : musicAlbum.getArtistName() ;
			String album = task.getAlbum() != null ? task.getAlbum() : musicAlbum.getName() ;
			
			MusicArtist artist = MusicManager.getInstance().getOrCreateArtist(albumArtist);
			Path albumPath = MusicManager.getInstance().getPath(artist.getName(), album);
	
			// FIXME : compressed is hardcoded
			musicAlbum = MusicManager.getInstance().getAlbum( artist.getName(), album, DownloadableStatus.DOWNLOADED, albumPath, MusicQuality.COMPRESSED, true );

			downloadableUtilsDAO.updateDownloadableId( musicFile.getFileId(), musicAlbum.getId() );
		}
		
		musicDAO.updateMusicFile(
				task.getMusicFile().getFileId(),
				musicFile.getSongTitle(),
				task.getSongArtist() != null ? task.getSongArtist() : musicFile.getSongArtist(),
				musicFile.getYear(),
				false );

		BackLogProcessor.getInstance().schedule( new SynchronizeMusicTagsTask(musicFile.getFilePath()), false);
	}

}
