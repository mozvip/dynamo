package dynamo.backlog.tasks.music;

import java.nio.file.Files;
import java.util.List;

import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.files.DeleteTask;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.model.TaskExecutor;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicFile;
import dynamo.music.jdbi.MusicAlbumDAO;

public class DeleteMusicFileExecutor extends TaskExecutor<DeleteMusicFileTask> {
	
	private MusicAlbumDAO musicDAO = null;

	public DeleteMusicFileExecutor(DeleteMusicFileTask task, MusicAlbumDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {
		if (Files.exists(task.getMusicFile().getPath())) {
			queue( new DeleteTask( task.getMusicFile().getPath(), true ), false );
		}
		musicDAO.deleteMusicFile( task.getMusicFile().getPath() );
		List<MusicFile> remainingMusicFiles = musicDAO.findMusicFiles( task.getMusicFile().getAlbumId() );
		if (remainingMusicFiles.size() == 0) {
			MusicAlbum album = task.getMusicAlbum();
			if (album == null) {
				album = (MusicAlbum) DownloadableFactory.getInstance().createInstance(task.getMusicFile().getAlbumId(), MusicAlbum.class);
			}
			queue( new DeleteDownloadableTask( album ), false );
		}
	}

}
