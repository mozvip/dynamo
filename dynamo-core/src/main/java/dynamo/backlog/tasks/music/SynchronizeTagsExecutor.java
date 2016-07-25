package dynamo.backlog.tasks.music;

import java.util.List;

import dynamo.core.manager.DAOManager;
import dynamo.core.model.TaskExecutor;
import dynamo.model.music.MusicFile;
import dynamo.music.jdbi.MusicAlbumDAO;

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
				queue( new SynchronizeMusicTagsTask(musicFile.getPath()), false );
			}
		}
	}

}
