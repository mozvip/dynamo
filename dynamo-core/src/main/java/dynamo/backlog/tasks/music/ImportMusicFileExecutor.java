package dynamo.backlog.tasks.music;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.images.Artwork;

import core.RegExp;
import dynamo.backlog.BackLogProcessor;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.LocalImageCache;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicQuality;
import dynamo.webapps.acoustid.AcoustId;

public class ImportMusicFileExecutor extends TaskExecutor<ImportMusicFileTask> {

	public ImportMusicFileExecutor(ImportMusicFileTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {

		Path musicFilePath = task.getPath();
		Path folder = musicFilePath.getParent();

		AudioFile audioFile;
		try {
			audioFile = AudioFileIO.read( musicFilePath.toFile() );
		} catch (CannotReadException e) {
			ErrorManager.getInstance().reportWarning(task, String.format("Cannot retrieve metadata from sound file %s", musicFilePath.toString()), true);
			return;
		}

		Tag audioTag = audioFile.getTag();

		if (audioTag != null) {

			String artistName = audioTag.getFirst(FieldKey.ALBUM_ARTIST);
			String albumName = audioTag.getFirst(FieldKey.ALBUM);
			String songTitle = audioTag.getFirst(FieldKey.TITLE);
			String songArtist = audioTag.getFirst(FieldKey.ARTIST);
			String yearStr = audioTag.getFirst(FieldKey.YEAR);
			String trackStr = audioTag.getFirst(FieldKey.TRACK);
			
			int year = 0, track = 0;

			if (yearStr != null && RegExp.matches( yearStr, "\\d+" )) {
				year = Integer.parseInt( yearStr );
			}
			if (trackStr != null && RegExp.matches( trackStr, "\\d+" )) {
				track = Integer.parseInt( trackStr );
			}
			
			if (StringUtils.isBlank( albumName)) {
				albumName = "<Unknown>";
			}

			String targetCoverImage = String.format("albums/%s.jpg", MusicManager.getSearchString(artistName, albumName));
			Path folderImage = folder.resolve("folder.jpg");
			String localImage = null;
			if (!LocalImageCache.getInstance().exists(targetCoverImage)) {
				if (Files.exists( folderImage )) {
					localImage = LocalImageCache.getInstance().download( targetCoverImage, Files.readAllBytes( folderImage ) );
				} else {
					Artwork artwork = audioTag.getFirstArtwork();
					if ( artwork != null ) {
						// FIXME: .jpg is hardcoded
						localImage = LocalImageCache.getInstance().download( targetCoverImage, artwork.getBinaryData() );
						// save to folder.jpg
						Files.write( folderImage, artwork.getBinaryData() );
					}
				}
			}
			
			if (StringUtils.isBlank(songArtist)) {
				songArtist = audioTag.getFirst(FieldKey.ORIGINAL_ARTIST);
			}
			
			MusicAlbum musicAlbum = task.getMusicAlbum();
			if (musicAlbum != null) {
				artistName = musicAlbum.getArtistName();
				albumName = musicAlbum.getAlbum();
			} else {
				musicAlbum = MusicManager.getInstance().getAlbum(
						artistName, albumName, null, localImage, DownloadableStatus.DOWNLOADED,
						MusicManager.getInstance().getPath( artistName, albumName ), audioTag instanceof FlacTag ? MusicQuality.LOSSLESS : MusicQuality.COMPRESSED, true);
			}

			try {
				MusicManager.getInstance().newMusicFile( musicFilePath, musicAlbum, songTitle, songArtist, track, year, task.isKeepSourceFile() );
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( task, String.format("Error while trying to import %s : %s", musicFilePath.toString(), e.getClass().getName()), e);
			}
			
			if (AcoustId.getInstance().isEnabled()) {
				String acoustId = audioTag.getFirst(FieldKey.ACOUSTID_ID);
				if (StringUtils.isBlank( acoustId)) {
					BackLogProcessor.getInstance().schedule( new CalcAcoustIdTask( musicFilePath ), false );
				}
			}
		} else {
			if (AcoustId.getInstance().isEnabled()) {
				BackLogProcessor.getInstance().schedule(new IdentifyMusicFileTask( musicFilePath ), false);
			}
		}
	}

}
