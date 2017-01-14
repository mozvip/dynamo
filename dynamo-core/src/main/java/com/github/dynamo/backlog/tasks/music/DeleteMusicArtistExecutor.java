package com.github.dynamo.backlog.tasks.music;

import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.files.DeleteDownloadableTask;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;

public class DeleteMusicArtistExecutor extends TaskExecutor<DeleteMusicArtistTask> {
	
	private MusicAlbumDAO musicDAO;

	public DeleteMusicArtistExecutor(DeleteMusicArtistTask task, MusicAlbumDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {
		List<MusicAlbum> albums = musicDAO.findAllAlbumsForArtist( task.getArtistName() );
		if (albums != null) {
			for (MusicAlbum album : albums) {
				// FIXME : do we want to physically remove the files as well ?
				BackLogProcessor.getInstance().schedule( new DeleteDownloadableTask( album ), false );
			}
		}
		musicDAO.deleteArtist( task.getArtistName() );
	}

}