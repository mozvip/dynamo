package dynamo.webapps.predb;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PreDBService {
	
	@GET("/")
	Call<ResponseBody> getResultsForCatsTagAndPage(@Query("cats") String cats, @Query("tag") String tag, @Query("page") int page);

}
