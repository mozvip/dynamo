package com.github.dynamo.backlog.tasks.music;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.model.music.MusicFile;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;

public class SetAlbumImageExecutor extends TaskExecutor<SetAlbumImageTask> {
	
	private MusicAlbumDAO musicDAO;

	public SetAlbumImageExecutor(SetAlbumImageTask task, MusicAlbumDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {
		
		long albumId = task.getMusicAlbum().getId();
		
		List<MusicFile> files = musicDAO.findMusicFiles( albumId );
		if (files != null && files.size() > 0) {
			for (MusicFile file : files) {
				BackLogProcessor.getInstance().schedule( new SynchronizeMusicTagsTask( file.getFilePath() ), false );	// update file tag
			}
		}

		Path albumPath = task.getMusicAlbum().getFolder();
		if (albumPath != null) {
			Path folderJpg = albumPath.resolve("folder.jpg");
			if (!Files.exists( albumPath )) {
				Files.createDirectories( albumPath );
			}
			Files.copy( task.getLocalImagePath(), folderJpg, StandardCopyOption.REPLACE_EXISTING );
		}
	}

}
