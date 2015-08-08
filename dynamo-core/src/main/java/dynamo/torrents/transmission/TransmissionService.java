package dynamo.torrents.transmission;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

public interface TransmissionService {
	
	@GET("/rpc")
	String init();
	
	@POST("/rpc")
	TransmissionResponse sendRequest(@Body TransmissionRequest request);
	

}
