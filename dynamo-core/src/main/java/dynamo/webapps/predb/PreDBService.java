package dynamo.webapps.predb;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

public interface PreDBService {
	
	@GET("/")
	Response getResultsForCatsTagAndPage(@Query("cats") String cats, @Query("tag") String tag, @Query("page") int page);

}
