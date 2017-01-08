package com.github.dynamo.backlog.tasks.music;

import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.files.DeleteDownloadableTask;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;

public class BlackListArtistTaskExecutor extends TaskExecutor<BlackListArtistTask> {

	private MusicAlbumDAO musicDAO;

	public BlackListArtistTaskExecutor(BlackListArtistTask task, MusicAlbumDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {
		musicDAO.blackList( task.getArtistName() );
		List<MusicAlbum> albums = musicDAO.findAllAlbumsForArtist( task.getArtistName() );
		if (albums != null) {
			for (MusicAlbum album : albums) {
				BackLogProcessor.getInstance().schedule( new DeleteDownloadableTask( album ));
			}
		}
	}

}
