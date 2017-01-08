package com.github.dynamo.webapps.thegamesdb.net;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


public class TheGamesDB {
	
	private TheGamesDBService service = null;

	private TheGamesDB() {
		Retrofit retrofit = new Retrofit.Builder().baseUrl( "http://thegamesdb.net" ).addConverterFactory( SimpleXmlConverterFactory.create() ).build();
		service = retrofit.create(TheGamesDBService.class);
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

	public TheGamesDBGame getGame(Long id) {
		GetGamesListResponse response = service.getGame(id);
		if (response.getGames() != null && response.getGames().size() > 0) {
			return response.getGames().get(0);
		}
		return null;
	}

	public GetArtResponse getArt(long id) {
		return service.getArt(id);
	}

}
