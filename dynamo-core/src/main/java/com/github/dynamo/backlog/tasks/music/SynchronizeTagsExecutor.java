package com.github.dynamo.backlog.tasks.music;

import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.model.music.MusicFile;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;

public class SynchronizeTagsExecutor extends TaskExecutor<SynchronizeTagsTask> {
	
	private static MusicAlbumDAO musicDAO = DAOManager.getInstance().getDAO( MusicAlbumDAO.class );

	public SynchronizeTagsExecutor(SynchronizeTagsTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		List<MusicFile> files = musicDAO.findModifiedTags();
		if ( files != null ) {
			for (MusicFile musicFile : files) {
				BackLogProcessor.getInstance().schedule( new SynchronizeMusicTagsTask(musicFile.getFilePath()), false );
			}
		}
	}

}
