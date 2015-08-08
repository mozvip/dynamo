package dynamo.webapps.thegamesdb.net;

import hclient.RetrofitClient;
import retrofit.RestAdapter;
import retrofit.converter.SimpleXMLConverter;


public class TheGamesDB {
	
	private TheGamesDBService service = null;

	private TheGamesDB() {
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint( "http://thegamesdb.net" ).setConverter(new SimpleXMLConverter()).setClient( new RetrofitClient() ).build();
		service = restAdapter.create(TheGamesDBService.class);
	}

	static class SingletonHolder {
		static TheGamesDB instance = new TheGamesDB();
	}

	public static TheGamesDB getInstance() {
		return SingletonHolder.instance;
	}

	public GetPlatformsListResponse getPlatformsList() {
		return service.getPlatformsList();
	}

	public GetGamesListResponse getGamesList(String name, String platform, String genre) {
		return service.getGamesList(name, platform, genre);
	}

	public GetGamesListResponse getGame(String name, Long id) {
		return service.getGame(name, id);
	}

	public GetArtResponse getArt(long id) {
		return service.getArt(id);
	}

}
