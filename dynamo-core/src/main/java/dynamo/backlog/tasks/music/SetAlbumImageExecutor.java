package dynamo.backlog.tasks.music;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.model.music.MusicFile;
import dynamo.music.jdbi.MusicAlbumDAO;

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
		
		DownloadableManager.downloadImage( task.getMusicAlbum(), task.getLocalImagePath() );
		List<MusicFile> files = musicDAO.findMusicFiles( albumId );
		if (files != null && files.size() > 0) {
			for (MusicFile file : files) {
				if (albumPath == null) {
					albumPath = file.getPath().getParent();
				}
				queue( new SynchronizeMusicTagsTask( file.getPath() ), false );	// update file tag
			}
		}

		if (albumPath != null) {
			Path folderJpg = albumPath.resolve("folder.jpg");
			Files.copy( task.getLocalImagePath(), folderJpg, StandardCopyOption.REPLACE_EXISTING );
		}
	}

}
