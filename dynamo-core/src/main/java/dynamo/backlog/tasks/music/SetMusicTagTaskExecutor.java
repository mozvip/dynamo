package dynamo.backlog.tasks.music;

import java.nio.file.Files;
import java.nio.file.Path;

import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.files.MoveFileTask;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicFile;
import dynamo.model.music.MusicQuality;
import dynamo.music.jdbi.MusicAlbumDAO;

public class SetMusicTagTaskExecutor extends TaskExecutor<SetMusicTagTask> {
	
	private MusicAlbumDAO musicDAO = null;

	public SetMusicTagTaskExecutor(SetMusicTagTask task, MusicAlbumDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {

		MusicFile musicFile = task.getMusicFile();
		
		long previousAlbumId = musicFile.getAlbumId();
		long newAlbumId = musicFile.getAlbumId();

		MusicAlbum musicAlbum = musicDAO.find( previousAlbumId );
		
		Path albumPath = musicAlbum.getFolder();

		if ( task.getAlbumArtist() != null || task.getAlbum() != null ) {
			String albumArtist = task.getAlbumArtist() != null ? task.getAlbumArtist() : musicAlbum.getArtistName() ;
			String album = task.getAlbum() != null ? task.getAlbum() : musicAlbum.getName() ;
			
			albumPath = MusicManager.getInstance().getPath(albumArtist, album);
	
			// FIXME : compressed is hardcoded
			musicAlbum = MusicManager.getInstance().getAlbum( albumArtist, album, null, DownloadableStatus.DOWNLOADED, albumPath, MusicQuality.COMPRESSED, true );
			newAlbumId = musicAlbum.getId();
		}
		
		Path theoriticalFilePath = albumPath.resolve( musicFile.getFileName() );
		if (!musicFile.getPath().equals(theoriticalFilePath)) {
			if (!Files.exists(theoriticalFilePath) || Files.size(theoriticalFilePath) < musicFile.getSize()) {
				// FIXME : what to do if destination file exists ? only keep best quality ?
				runSync( new MoveFileTask( musicFile.getPath(), theoriticalFilePath, musicAlbum) );
			} else {
				queue( new DeleteMusicFileTask( musicFile ), false );
			}

			musicDAO.deleteMusicFile(musicFile.getPath());
		}
		
		musicDAO.createMusicFile(
				theoriticalFilePath,
				newAlbumId,
				musicFile.getSongTitle(),
				task.getSongArtist() != null ? task.getSongArtist() : musicFile.getSongArtist(),
				musicFile.getTrack(), musicFile.getYear(), musicFile.getSize(), true);
		
		if (previousAlbumId != newAlbumId) {
			if (musicDAO.getMusicFilesCount(previousAlbumId) == 0) {
				queue( new DeleteDownloadableTask( previousAlbumId ));
			}
		}
		
		queue( new SynchronizeMusicTagsTask(theoriticalFilePath), false);
	}

}
