package dynamo.webapps.thegamesdb.net;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.webapps.thegamesdb.net.images.TheGamesDBBoxArt;

public class TheGamesDBTest {
	
	private static TheGamesDB service;
	
	@BeforeClass
	public static void init() {
		service = TheGamesDB.getInstance();
	}

	@Test
	public void testGetPlatformsList() {
		GetPlatformsListResponse response = service.getPlatformsList();
		for (TheGamesDBPlatform platform : response.getPlatforms()) {
			System.out.println( platform.getName() );
		}
	}

	@Test
	public void testGetGamesList() {
		GetGamesListResponse response = service.getGamesList("Silent Hill", null, null);
		for (TheGamesDBGame game : response.getGames()) {
			System.out.println( game.getGameTitle() + " - " + game.getPlatform() );
		}
	}

	@Test
	public void testGetGame() {
		GetGamesListResponse response = service.getGame("Silent Hill", null);
		for (TheGamesDBGame game : response.getGames()) {
			System.out.println( game.getGameTitle() + " - " + game.getPlatform() );
		}		
	}

	@Test
	public void testGetGameById() {
		GetGamesListResponse response = service.getGame( null, 11l );
		for (TheGamesDBGame game : response.getGames()) {
			System.out.println( game.getGameTitle() + " - " + game.getPlatform() );
		}		
	}

	@Test
	public void testGetArt() {
		GetArtResponse response = service.getArt(11);
		for (TheGamesDBBoxArt boxart : response.getImages().getBoxarts()) {
			System.out.println( boxart.getPath() );
		}		
	}

}
