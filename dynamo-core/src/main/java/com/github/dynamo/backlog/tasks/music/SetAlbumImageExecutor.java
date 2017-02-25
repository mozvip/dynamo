package com.github.dynamo.backlog.tasks.music;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.github.dynamo.core.model.TaskExecutor;
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
