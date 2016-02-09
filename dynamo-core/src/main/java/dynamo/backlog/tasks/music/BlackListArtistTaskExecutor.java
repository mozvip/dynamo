package dynamo.backlog.tasks.music;

import java.util.List;

import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.MusicDAO;
import dynamo.model.music.MusicAlbum;

public class BlackListArtistTaskExecutor extends TaskExecutor<BlackListArtistTask> {

	private MusicDAO musicDAO;

	public BlackListArtistTaskExecutor(BlackListArtistTask task, MusicDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {
		musicDAO.blackList( task.getArtistName() );
		List<MusicAlbum> albums = musicDAO.findAllAlbumsForArtist( task.getArtistName() );
		if (albums != null) {
			for (MusicAlbum album : albums) {
				queue( new DeleteDownloadableTask( album ));
			}
		}
	}

}
