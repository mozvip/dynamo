package dynamo.webapps.pushbullet;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;

public interface PushBulletService {
	
	@FormUrlEncoded
	@POST("/v2/pushes")
	public PushBulletReponse push(@Header("Authorization") String accessToken, @Field("device_iden") String device_iden , @Field("type") String type, @Field("title") String title, @Field("body") String body , @Field("url") String url);
	
	@GET("/v2/devices")
	public PushBulletReponse getDevices(@Header("Authorization") String accessToken);

}
