package dynamo.webapps.acoustid;

import retrofit.http.GET;
import retrofit.http.Query;

public interface AcoustIDService {
	
	@GET("/v2/lookup?meta=recordings+releases+releasegroups+tracks+compress+usermeta+sources")
	AcoustIdLookupResults lookup(@Query("client") String client, @Query("duration") int duration, @Query("fingerprint") String fingerprint);

	@GET("/v2/lookup?meta=releases")
	AcoustIdLookupResults lookupReleases(@Query("client") String client, @Query("duration") int duration, @Query("fingerprint") String fingerprint);

	@GET("/v2/lookup?meta=recordings+releases+releaseids+releasegroups+tracks+compress+usermeta+sources")
	AcoustIdLookupResults lookup(@Query("client") String client, @Query("trackid") String trackid);

}
