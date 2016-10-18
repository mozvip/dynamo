package dynamo.webapps.theaudiodb;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TheAudioDBService {

	@GET("/api/v1/json/{apiKey}/searchalbum.php")
	AudioDBResponse searchAlbum(@Path("apiKey") String apiKey, @Query("s") String artistName, @Query("a") String albumName);

}
