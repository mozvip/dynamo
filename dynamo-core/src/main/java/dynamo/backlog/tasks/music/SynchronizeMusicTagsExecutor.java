package dynamo.backlog.tasks.music;

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

import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicFile;
import dynamo.music.jdbi.MusicAlbumDAO;

public class SynchronizeMusicTagsExecutor extends TaskExecutor<SynchronizeMusicTagsTask> {
	
	private MusicAlbumDAO musicDAO;

	public SynchronizeMusicTagsExecutor(SynchronizeMusicTagsTask task, MusicAlbumDAO musicDAO) {
		super(task);
		this.musicDAO = musicDAO;
	}

	@Override
	public void execute() throws Exception {

		MusicFile musicFile = musicDAO.findMusicFile( task.getMusicFilePath() );
		
		AudioFile audioFile;
		try {
			audioFile = AudioFileIO.read( musicFile.getPath().toFile() );
		} catch (java.io.FileNotFoundException e) {
			musicDAO.deleteMusicFile( musicFile.getPath() );
			throw e;
		}

		Tag audioTag = audioFile.getTagOrCreateDefault();
		if (audioTag instanceof ID3v1Tag) {
			audioTag = ((MP3File) audioFile).convertTag(audioTag, ID3V2Version.ID3_V24);
		}	

		MusicAlbum album = musicDAO.find( musicFile.getAlbumId() );
		
		audioTag.setField(FieldKey.ALBUM, album.getName() );
		audioTag.setField(FieldKey.ALBUM_ARTIST, album.getArtistName() );
		audioTag.setField(FieldKey.ARTIST, musicFile.getSongArtist() );
		audioTag.setField(FieldKey.TITLE, musicFile.getSongTitle() );
		audioTag.setField(FieldKey.TRACK, "" + musicFile.getTrack() );
		if (musicFile.getYear() > 0) {
			audioTag.setField(FieldKey.YEAR, "" + musicFile.getYear() );
		}

		Path p = DownloadableManager.resolveImage( album );
		if ( p != null ) {
			Artwork artwork = ArtworkFactory.createArtworkFromFile(p.toFile());
			audioTag.setField(artwork);
		}

		audioFile.commit();
		
		musicDAO.updateTagsModified( musicFile.getPath(), false );
	}

}
