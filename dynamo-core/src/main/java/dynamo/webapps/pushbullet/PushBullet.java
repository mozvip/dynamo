package dynamo.webapps.pushbullet;

import java.io.IOException;
import java.util.List;

import com.github.mozvip.pushbullet.PushBulletClient;
import com.github.mozvip.pushbullet.model.PushBulletDevice;

import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;

public class PushBullet implements Enableable, Reconfigurable {
	
	@Configurable
	private String accessToken;
	
	@Configurable
	private String deviceIdent;
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getDeviceIdent() {
		return deviceIdent;
	}
	
	public void setDeviceIdent(String deviceIdent) {
		this.deviceIdent = deviceIdent;
	}

	public boolean isEnabled() {
		return accessToken != null && deviceIdent != null;
	}

	private PushBullet() {
	}

	static class SingletonHolder {
		static PushBullet instance = new PushBullet();
	}

	public static PushBullet getInstance() {
		return SingletonHolder.instance;
	}

	private PushBulletClient client;

	@Override
	public void reconfigure() {
		client = PushBulletClient.Builder().accessToken(accessToken).build();
	}

	public void pushNote(String title, String body) throws IOException {
		client.pushNote(deviceIdent, title, body);
	}

	public void pushLink(String title, String body, String url) throws IOException {
		client.pushLink(deviceIdent, title, body, url);
	}

	public List<PushBulletDevice> getDevices() throws IOException {
		return client.getDevices();
	}
	
	

}
