package com.github.dynamo.backlog.tasks.music;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;
import com.github.mozvip.acoustid.AcoustIdClient;
import com.github.mozvip.acoustid.AcoustIdLookupResults;
import com.github.mozvip.hclient.core.RegExp;

public class IdentifyMusicFileExecutor extends TaskExecutor<IdentifyMusicFileTask> {
	
	private DownloadableUtilsDAO downloadableUtilsDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );
	private MusicAlbumDAO musicDAO = DAOManager.getInstance().getDAO( MusicAlbumDAO.class );

	public IdentifyMusicFileExecutor(IdentifyMusicFileTask task) {
		super(task);
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
		
		AcoustIdLookupResults results = AcoustId.getInstance().lookup( acoustId );
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

		MusicAlbum album = MusicManager.getInstance().getAlbum( albumArtist, albumName, DownloadableStatus.DOWNLOADED, null, MusicQuality.COMPRESSED, true );

		// delete existing files if exists
		downloadableUtilsDAO.deleteFile( task.getMusicFilePath() );
		
		if (album != null) {
			long fileId = DownloadableManager.getInstance().addFile( album, task.getMusicFilePath(), track );
			musicDAO.createMusicFile( fileId, audioTag.getFirst(FieldKey.TITLE), audioTag.getFirst(FieldKey.ARTIST), year, false );
		}

	}

}