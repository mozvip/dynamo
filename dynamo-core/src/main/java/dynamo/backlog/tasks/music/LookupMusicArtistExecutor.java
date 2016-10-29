package dynamo.backlog.tasks.music;

import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.core.model.TaskExecutor;
import dynamo.model.music.MusicArtist;
import dynamo.music.jdbi.MusicAlbumDAO;
import dynamo.webapps.theaudiodb.TheAudioDB;

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

		TheAudioDB.getInstance().searchAlbums( artist.getName() );
		
	}

}
