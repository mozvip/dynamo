package dynamo.music;

import java.io.IOException;

import dynamo.core.configuration.ClassDescription;
import fr.mozvip.theaudiodb.TheAudioDbClient;
import fr.mozvip.theaudiodb.model.AudioDbResponse;

@ClassDescription(label="TheAudioDB")
public class TheAudioDb {
	
	private TheAudioDbClient service = null;

	private TheAudioDb() {
		service = TheAudioDbClient.Builder().build();
	}

	static class SingletonHolder {
		static TheAudioDb instance = new TheAudioDb();
	}

	public static TheAudioDb getInstance() {
		return SingletonHolder.instance;
	}

	public AudioDbResponse searchArtist(String artistName) throws IOException {
		return service.searchArtist(artistName);
	}

	public AudioDbResponse searchAlbum(String artistName, String albumName) throws IOException {
		return service.searchAlbum(artistName, albumName);
	}

	public AudioDbResponse searchAlbums(String artistName) throws IOException {
		return service.searchAlbums(artistName);
	}

	public AudioDbResponse getAlbum(long tadbAlbumId) throws IOException {
		return service.getAlbum(tadbAlbumId);
	}
	


}
