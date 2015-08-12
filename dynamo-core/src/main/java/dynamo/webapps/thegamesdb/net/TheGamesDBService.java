package dynamo.webapps.thegamesdb.net;

import retrofit.http.GET;
import retrofit.http.Query;

public interface TheGamesDBService {
	
	@GET("/api/GetPlatformsList.php")
	public GetPlatformsListResponse getPlatformsList();
	
	@GET("/api/GetGamesList.php")
	public GetGamesListResponse getGamesList( @Query("name") String name, @Query("platform") String platform, @Query("genre") String genre );
	
	@GET("/api/GetGame.php")
	public GetGamesListResponse getGame( @Query("name") String name, @Query("id") Long id );
	
	@GET("/api/GetGame.php")
	public GetGamesListResponse getGame( @Query("id") Long id );

	@GET("/api/GetArt.php")
	public GetArtResponse getArt( @Query("id") Long id );

}
