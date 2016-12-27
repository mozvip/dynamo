package dynamo.backlog.tasks.music;

import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.core.model.TaskExecutor;
import dynamo.model.music.MusicArtist;
import dynamo.music.TheAudioDb;
import dynamo.music.jdbi.MusicAlbumDAO;

public class LookupMusicArtistExecutor extends TaskExecutor<LookupMusicArtistTask> {
	
	private MusicAlbumDAO musicDAO;
	private DownloadableUtilsDAO downloadableDAO;
	
	public LookupMusicArtistExecutor(LookupMusicArtistTask item, MusicAlbumDAO musicDAO, DownloadableUtilsDAO downloadableDAO) {
		super(item);
		this.musicDAO = musicDAO;
		this.downloadableDAO = downloadableDAO;
	}

	@Override
	public void execute() throws Exception {
		
		MusicArtist artist = task.getArtist();
		
		// FIXME

		TheAudioDb.getInstance().searchAlbums( artist.getName() );
		
	}

}
