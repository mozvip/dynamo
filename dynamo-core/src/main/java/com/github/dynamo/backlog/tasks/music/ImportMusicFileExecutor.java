package com.github.dynamo.backlog.tasks.music;

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

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.files.MoveFileTask;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;
import com.github.mozvip.acoustid.AcoustIdClient;
import com.github.mozvip.hclient.core.RegExp;

public class ImportMusicFileExecutor extends TaskExecutor<ImportMusicFileTask> {
	
	protected String[] intermediateFolders = new String[] {"Disc 1", "CD 1", "CD1", "Disc 2", "CD 2", "CD2"};
	private MusicAlbumDAO musicDAO = DAOManager.getInstance().getDAO( MusicAlbumDAO.class );
	private DownloadableUtilsDAO downloadableUtilsDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );

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

			Path folderImage = folder.resolve("folder.jpg");
			if (!Files.exists( folderImage)) {
				Artwork artwork = audioTag.getFirstArtwork();
				if ( artwork != null ) {
					// FIXME: .jpg is hardcoded
					// save to folder.jpg
					Files.write( folderImage, artwork.getBinaryData() );
				}
			}

			if (StringUtils.isBlank(songArtist)) {
				songArtist = audioTag.getFirst(FieldKey.ORIGINAL_ARTIST);
			}
			
			Path albumDestinationFolder = folder;

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
			
			MusicAlbum musicAlbum = MusicManager.getInstance().getAlbum(
					artistName, albumName, DownloadableStatus.DOWNLOADED,
					albumDestinationFolder, audioTag instanceof FlacTag ? MusicQuality.LOSSLESS : MusicQuality.COMPRESSED);
			
			if (Files.exists( folderImage)) {
				DownloadableManager.getInstance().downloadImage(musicAlbum, folderImage);
			} else {
				BackLogProcessor.getInstance().schedule( new FindMusicAlbumImageTask( musicAlbum ) );
			}

			albumDestinationFolder = musicAlbum.getFolder();

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
		
		long fileId = DownloadableManager.getInstance().addFile( musicAlbum, musicFilePath, track );
		musicDAO.createMusicFile( fileId, songTitle, songArtist, year, false );
	}	

}
