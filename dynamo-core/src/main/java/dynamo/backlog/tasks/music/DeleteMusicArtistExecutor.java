package dynamo.backlog.tasks.music;

import java.util.List;

import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.MusicDAO;
import dynamo.model.music.MusicAlbum;

public class DeleteMusicArtistExecutor extends TaskExecutor<DeleteMusicArtistTask> {
	
	private MusicDAO musicDAO;

	public DeleteMusicArtistExecutor(DeleteMusicArtistTask task, MusicDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {
		List<MusicAlbum> albums = musicDAO.findAllAlbumsForArtist( task.getArtistName() );
		if (albums != null) {
			for (MusicAlbum album : albums) {
				// FIXME : do we want to physically remove the files as well ?
				queue( new DeleteDownloadableTask( album ), false );
			}
		}
		musicDAO.deleteArtist( task.getArtistName() );
	}

}
