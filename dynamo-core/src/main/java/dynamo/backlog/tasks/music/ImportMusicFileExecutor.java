package dynamo.backlog.tasks.music;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

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
import dynamo.backlog.tasks.files.MoveFileTask;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicQuality;
import dynamo.music.jdbi.MusicAlbumDAO;
import dynamo.webapps.acoustid.AcoustId;

public class ImportMusicFileExecutor extends TaskExecutor<ImportMusicFileTask> {
	
	protected String[] intermediateFolders = new String[] {"Disc 1", "CD 1", "CD1", "Disc 2", "CD 2", "CD2"};
	private MusicAlbumDAO musicDAO = DAOManager.getInstance().getDAO( MusicAlbumDAO.class );

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
			
			if (StringUtils.isBlank( artistName)) {
				artistName = "<Unknown Artist>";
			}
			if (StringUtils.isBlank( albumName)) {
				albumName = "<Unknown>";
			}

			String localImage = String.format("albums/%s.jpg", MusicManager.getSearchString(artistName, albumName));
			Path folderImage = folder.resolve("folder.jpg");
			if (LocalImageCache.getInstance().missFile(localImage)) {
				if (Files.exists( folderImage )) {
					LocalImageCache.getInstance().download( localImage, Files.readAllBytes( folderImage ) );
				} else {
					Artwork artwork = audioTag.getFirstArtwork();
					if ( artwork != null ) {
						// FIXME: .jpg is hardcoded
						LocalImageCache.getInstance().download( localImage, artwork.getBinaryData() );
						// save to folder.jpg
						Files.write( folderImage, artwork.getBinaryData() );
					}
				}
			}
			
			if (StringUtils.isBlank(songArtist)) {
				songArtist = audioTag.getFirst(FieldKey.ORIGINAL_ARTIST);
			}
			
			Path albumDestinationFolder = folder;

			MusicAlbum musicAlbum = task.getMusicAlbum();
			if (musicAlbum != null) {
				artistName = musicAlbum.getArtistName();
				albumName = musicAlbum.getName();
				
				albumDestinationFolder = musicAlbum.getPath();
			} else {

				boolean importInPlace = false;
				for (Path configuredFolder : MusicManager.getInstance().getFolders()) {
					if (albumDestinationFolder.startsWith(configuredFolder)) {
						importInPlace = true;
						break;
					}
				}
				
				if (importInPlace) {
					for (String folderName : intermediateFolders) {
						if (albumDestinationFolder.getFileName().toString().equalsIgnoreCase(folderName)) {
							albumDestinationFolder = albumDestinationFolder.getParent();
						}
					}
				}
				
				musicAlbum = MusicManager.getInstance().getAlbum(
						artistName, albumName, null, DownloadableStatus.DOWNLOADED,
						albumDestinationFolder, audioTag instanceof FlacTag ? MusicQuality.LOSSLESS : MusicQuality.COMPRESSED, true);
			}
			
			if (albumDestinationFolder == null) {
				// default destination folder
				albumDestinationFolder = MusicManager.getInstance().getPath(artistName, albumName);
			}

			try {
				newMusicFile( musicFilePath, albumDestinationFolder, musicAlbum, songTitle, songArtist, track, year, task.isKeepSourceFile() );
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
	
	protected void newMusicFile( Path musicFilePath, Path albumDestinationFolder, MusicAlbum musicAlbum, String songTitle, String songArtist, int track, int year, boolean keepSourceFiles ) throws IOException, ExecutionException {
		Path targetPath = musicFilePath;
		if (!keepSourceFiles) {
			// we are allowed to move this file to its final destination // FIXME: handle Disc %d parent folders
			targetPath = albumDestinationFolder.resolve(musicFilePath.getFileName()).toAbsolutePath();
			if ((!Files.exists(targetPath)) || (!Files.isSameFile(musicFilePath, targetPath))) {
				BackLogProcessor.getInstance().schedule( new MoveFileTask(musicFilePath, targetPath, musicAlbum), false );
			}
		}

		if (songTitle != null ) {
			songTitle = StringUtils.capitalize( songTitle ).trim();
		}
		musicDAO.createMusicFile( targetPath, musicAlbum.getId(), songTitle, songArtist, track, year, Files.size(musicFilePath), false );
		DownloadableManager.getInstance().addFile( musicAlbum, musicFilePath, track );
	}	

}
