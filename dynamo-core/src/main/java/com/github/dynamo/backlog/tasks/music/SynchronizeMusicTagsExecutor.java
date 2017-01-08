package com.github.dynamo.backlog.tasks.music;

import java.nio.file.Path;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jaudiotagger.tag.reference.ID3V2Version;

import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.model.music.MusicFile;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;

public class SynchronizeMusicTagsExecutor extends TaskExecutor<SynchronizeMusicTagsTask> {
	
	private DownloadableUtilsDAO downloadableUtilsDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );
	private MusicAlbumDAO musicDAO = DAOManager.getInstance().getDAO( MusicAlbumDAO.class );

	public SynchronizeMusicTagsExecutor(SynchronizeMusicTagsTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {

		MusicFile musicFile = musicDAO.findMusicFile( task.getMusicFilePath() );
		
		AudioFile audioFile;
		try {
			audioFile = AudioFileIO.read( musicFile.getFilePath().toFile() );
		} catch (java.io.FileNotFoundException e) {
			downloadableUtilsDAO.deleteFile( musicFile.getFilePath() );
			throw e;
		}

		Tag audioTag = audioFile.getTagOrCreateDefault();
		if (audioTag instanceof ID3v1Tag) {
			audioTag = ((MP3File) audioFile).convertTag(audioTag, ID3V2Version.ID3_V24);
		}	

		MusicAlbum album = musicDAO.find( musicFile.getDownloadableId() );
		
		audioTag.setField(FieldKey.ALBUM, album.getName() );
		audioTag.setField(FieldKey.ALBUM_ARTIST, album.getArtistName() );
		audioTag.setField(FieldKey.ARTIST, musicFile.getSongArtist() );
		audioTag.setField(FieldKey.TITLE, musicFile.getSongTitle() );
		audioTag.setField(FieldKey.TRACK, "" + musicFile.getIndex() );
		if (musicFile.getYear() > 0) {
			audioTag.setField(FieldKey.YEAR, "" + musicFile.getYear() );
		}

		Path p = DownloadableManager.resolveImage( album );
		if ( p != null ) {
			Artwork artwork = ArtworkFactory.createArtworkFromFile(p.toFile());
			audioTag.setField(artwork);
		}

		audioFile.commit();
		
		musicDAO.updateTagsModified( musicFile.getFileId(), false );
	}

}
