package dynamo.backlog.tasks.music;

import dynamo.core.model.TaskExecutor;
import dynamo.music.jdbi.MusicAlbumDAO;

public class MusicArtistToggleFavoriteExecutor extends TaskExecutor<MusicArtistToggleFavoriteTask> {
	
	private MusicAlbumDAO musicDAO;

	public MusicArtistToggleFavoriteExecutor(MusicArtistToggleFavoriteTask task, MusicAlbumDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {
		musicDAO.updateFavorite( task.getArtist().getName(), task.isFavorite() );
		if ( task.isFavorite() ) {
			queue( new LookupMusicArtistTask( task.getArtist() ), false );
		}
	}

}
