package dynamo.webapps.theaudiodb;

import dynamo.core.configuration.ClassDescription;
import hclient.RetrofitClient;
import retrofit.RestAdapter;

@ClassDescription(label="TheAudioDB")
public class TheAudioDB {
	
	private TheAudioDBService service = null;
	
	private static String API_KEY = "1";
	
	private TheAudioDB() {
		RestAdapter restAdapter = new RestAdapter.Builder()
				.setEndpoint("http://www.theaudiodb.com")
				.setClient( new RetrofitClient() ).build();
		service = restAdapter.create(TheAudioDBService.class);
	}

	static class SingletonHolder {
		static TheAudioDB instance = new TheAudioDB();
	}

	public static TheAudioDB getInstance() {
		return SingletonHolder.instance;
	}
	
	public AudioDBResponse searchAlbum(String artistName, String albumName) {
		return service.searchAlbum( API_KEY, artistName, albumName );
	}

	public AudioDBResponse searchAlbums(String artistName) {
		return service.searchAlbums( API_KEY, artistName );
	}

	public AudioDBResponse searchArtist(String artistName) {
		return service.searchArtist( API_KEY, artistName );
	}

	public AudioDBResponse getAlbum(long tadbAlbumId) {
		return service.getAlbum( API_KEY, tadbAlbumId );
	}


}
