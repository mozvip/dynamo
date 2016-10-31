package dynamo.webapps.sabnzbd;

import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

public interface SABNzbdService {

	@Multipart
	@POST("/sabnzbd/api")
	public SabNzbdResponse addNZBByFileUpload(@Part("output") TypedString output, @Part("mode") TypedString mode, @Part("nzbname") TypedString nzbname, @Part("nzbfile") TypedFile nzbfile, @Part("apikey") TypedString apiKey);

	@GET("/sabnzbd/api?mode=addurl")
	public SabNzbdResponse addNZBByURL(@Query("name") String url, @Query("nzbname") String niceName, @Query("apikey") String apiKey);

	@GET("/sabnzbd/api?mode=qstatus&output=json")
	public SabNzbdResponse getQueueStatus(@Query("apikey") String apiKey);

	@GET("/sabnzbd/api?mode=queue&output=json")
	public SabNzbdResponse getQueue(@Query("apikey") String apiKey);

	@GET("/sabnzbd/api?mode=queue&name=delete&del_files=1")
	public SabNzbdResponse delete(@Query("value") String id, @Query("apikey") String apiKey);

	@GET("/sabnzbd/api?mode=history&name=delete&del_files=1")
	public boolean deleteFromHistory(@Query("value") String id, @Query("apikey") String apiKey);

	@GET("/sabnzbd/api?mode=history&limit=1000&output=json")
	public SabNzbdResponse getHistory(@Query("apikey") String apiKey);

	@GET("/sabnzbd/api?mode=history&name=delete&value=failed&del_files=1")
	public String deleteFailed(@Query("apikey") String apiKey);

}
