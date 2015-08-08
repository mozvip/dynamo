package dynamo.backlog.tasks.music;

import java.nio.file.Path;
import java.util.List;

import dynamo.backlog.tasks.files.CopyFileTask;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.MusicDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.music.MusicFile;

public class SetAlbumImageExecutor extends TaskExecutor<SetAlbumImageTask> {
	
	private MusicDAO musicDAO;

	public SetAlbumImageExecutor(SetAlbumImageTask task, MusicDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {
		
		long albumId = task.getMusicAlbum().getId();
		
		DownloadableManager.getInstance().updateCoverImage( albumId, task.getLocalImagePath() );
		List<MusicFile> files = musicDAO.findMusicFiles( albumId );
		if (files != null && files.size() > 0) {
			for (MusicFile file : files) {
				queue( new SynchronizeMusicTagsTask( file.getPath() ), false );	// update file tag
			}
		}

		Path folderJpg = task.getMusicAlbum().getPath().resolve("folder.jpg");
		Path sourceImage = LocalImageCache.getInstance().resolveLocal( task.getLocalImagePath() );
		
		queue( new CopyFileTask(sourceImage, folderJpg, task.getMusicAlbum()), false );
	}

}
