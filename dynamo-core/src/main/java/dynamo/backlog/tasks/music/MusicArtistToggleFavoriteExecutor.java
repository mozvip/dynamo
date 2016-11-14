package dynamo.backlog.tasks.music;

import dynamo.backlog.BackLogProcessor;
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
			BackLogProcessor.getInstance().schedule( new LookupMusicArtistTask( task.getArtist() ), false );
		}
	}

}
