package dynamo.backlog.tasks.music;

import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import dynamo.backlog.tasks.core.ScanFolderExecutor;
import dynamo.backlog.tasks.core.AudioFileFilter;
import dynamo.backlog.tasks.files.DeleteFileTask;
import dynamo.core.manager.ErrorManager;
import dynamo.manager.MusicManager;
import dynamo.model.music.MusicFile;
import dynamo.music.jdbi.MusicAlbumDAO;

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
				queue( new DeleteFileTask( musicFile.getFilePath() ), false);
			}
		}

		DirectoryStream<Path> ds = Files.newDirectoryStream(path, AudioFileFilter.getInstance());
		for (Path currentPath : ds) {

			if (cancelled) {
				return;
			}

			if (Files.isDirectory(currentPath)) {
				parsePath(currentPath);
			} else {
				importMusicFile(currentPath);
			}
		}
	}

	private void importMusicFile(Path currentPath) {
		MusicFile musicFile = musicDAO.findMusicFile(currentPath);
		if (musicFile == null) {
			try {
				runSync(new ImportMusicFileTask( currentPath, keepSourceFiles ));
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
