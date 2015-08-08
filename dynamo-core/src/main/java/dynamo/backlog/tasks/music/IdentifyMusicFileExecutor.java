package dynamo.backlog.tasks.music;

import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import core.RegExp;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.MusicDAO;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicQuality;
import dynamo.webapps.acoustid.AcoustId;
import dynamo.webapps.acoustid.LookupResults;

public class IdentifyMusicFileExecutor extends TaskExecutor<IdentifyMusicFileTask> {
	
	private MusicDAO musicDAO = null;

	public IdentifyMusicFileExecutor(IdentifyMusicFileTask task, MusicDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {
		
		AudioFile audioFile = AudioFileIO.read( task.getMusicFilePath().toFile() );
		Tag audioTag = audioFile.getTagOrCreateDefault();
		String acoustId = audioTag.getFirst(FieldKey.ACOUSTID_ID);
		if (StringUtils.isBlank( acoustId)) {
			runSync( new CalcAcoustIdTask( task.getMusicFilePath() ) );
			audioFile = AudioFileIO.read( task.getMusicFilePath().toFile() );
			audioTag = audioFile.getTagOrCreateDefault();
			acoustId = audioTag.getFirst(FieldKey.ACOUSTID_ID);
		}

		if ( audioTag == null || StringUtils.isBlank( acoustId )) {
			return;
		}
		
		LookupResults results = AcoustId.getInstance().lookup( acoustId );
		AcoustId.getInstance().populateTag(results, audioTag, true);
		audioFile.commit();
		
		String trackStr = audioTag.getFirst(FieldKey.TRACK);
		String yearStr = audioTag.getFirst(FieldKey.YEAR);
		
		int year = 0, track = 0;
		
		if (yearStr != null && RegExp.matches( yearStr, "\\d+" )) {
			year = Integer.parseInt( yearStr );
		}
		if (trackStr != null && RegExp.matches( trackStr, "\\d+" )) {
			track = Integer.parseInt( trackStr );
		}
		
		String albumArtist = audioTag.getFirst(FieldKey.ALBUM_ARTIST);
		String albumName = audioTag.getFirst(FieldKey.ALBUM);

		MusicAlbum album = MusicManager.getInstance().getAlbum( albumArtist, albumName, null, null, DownloadableStatus.DOWNLOADED, null, MusicQuality.COMPRESSED, true );

		// delete existing files if exists
		musicDAO.deleteFile( task.getMusicFilePath() );
		
		if (album != null) {
			musicDAO.createMusicFile(
					task.getMusicFilePath(),
					album.getId(),
					audioTag.getFirst(FieldKey.TITLE),
					audioTag.getFirst(FieldKey.ARTIST),
					track,
					year,
					Files.size(task.getMusicFilePath()),
					false );
		}

	}

}
