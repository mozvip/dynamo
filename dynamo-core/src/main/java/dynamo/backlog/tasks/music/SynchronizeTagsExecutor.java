package dynamo.backlog.tasks.music;

import java.util.List;

import dynamo.core.manager.DAOManager;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.MusicDAO;
import dynamo.model.music.MusicFile;

public class SynchronizeTagsExecutor extends TaskExecutor<SynchronizeTagsTask> {
	
	private static MusicDAO musicDAO = DAOManager.getInstance().getDAO( MusicDAO.class );

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
