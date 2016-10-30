package dynamo.webapps.theaudiodb;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TheAudioDBService {

	@GET("/api/v1/json/{apiKey}/search.php")
	AudioDBResponse searchArtist(@Path("apiKey") String apiKey, @Query("s") String artistName );

	@GET("/api/v1/json/{apiKey}/searchalbum.php")
	AudioDBResponse searchAlbum(@Path("apiKey") String apiKey, @Query("s") String artistName, @Query("a") String albumName);

	@GET("/api/v1/json/{apiKey}/searchalbum.php")
	AudioDBResponse searchAlbums(@Path("apiKey") String apiKey, @Query("s") String artistName);

	@GET("/api/v1/json/{apiKey}/album.php")
	AudioDBResponse getAlbum(@Path("apiKey") String apiKey, @Query("m") long tadbAlbumId);

}
