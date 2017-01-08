package com.github.dynamo.backlog.tasks.music;

import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.AudioFileFilter;
import com.github.dynamo.backlog.tasks.core.ScanFolderExecutor;
import com.github.dynamo.backlog.tasks.files.DeleteFileTask;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.manager.FolderManager;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.music.MusicFile;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;

public class ImportMusicFolderExecutor extends ScanFolderExecutor<ImportMusicFolderTask> {

	boolean keepSourceFiles = false;

	private MusicAlbumDAO musicDAO;

	public ImportMusicFolderExecutor(ImportMusicFolderTask item, MusicAlbumDAO musicDAO) {
		super(item);
		keepSourceFiles = item.isKeepSourceFiles();
		this.musicDAO = musicDAO;
	}

	@Override
	public void parsePath(Path path) throws Exception {
		
		if (Files.isRegularFile(path)) {
			importMusicFile(path);
			return;
		}

		if (MusicManager.getInstance().isCleanDuringImport()) {
			MusicManager.getInstance().cleanFolder(path);
		}

		List<MusicFile> musicFilesInFolder = musicDAO.findFilesInFolder( path );
		for (MusicFile musicFile : musicFilesInFolder) {
			if (musicFile.getFilePath() != null && !Files.isReadable( musicFile.getFilePath() )) {
				BackLogProcessor.getInstance().schedule( new DeleteFileTask( musicFile.getFilePath() ), false);
			}
		}
		
		List<Path> audioFiles = FolderManager.getInstance().getContents(path, AudioFileFilter.getInstance(), true);
		for (Path audioFile : audioFiles) {
			if (cancelled) {
				return;
			}
			importMusicFile(audioFile);
			
		}
	}

	private void importMusicFile(Path musicFilePath) {
		MusicFile musicFile = musicDAO.findMusicFile(musicFilePath);
		if (musicFile == null) {
			try {
				runSync(new ImportMusicFileTask( musicFilePath, keepSourceFiles ));
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( task, e );
			}
		}
	}

	@Override
	public Filter<Path> getFileFilter() {
		return AudioFileFilter.getInstance();
	}

}
