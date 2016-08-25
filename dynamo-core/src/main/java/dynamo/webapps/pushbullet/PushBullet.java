package dynamo.webapps.pushbullet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import hclient.RetrofitClient;
import retrofit.RestAdapter;

public class PushBullet implements Enableable {
	
	@Configurable(category="Notifiers", name="Enable PushBullet notifications")
	private boolean enabled;

	@Configurable(category="Notifiers", name="PushBullet Access Token")
	private String accessToken;
	
	@Configurable(category="Notifiers", name="PushBullet Device")
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
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	private PushBulletService service = null;

	private PushBullet() {
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://api.pushbullet.com").setClient( new RetrofitClient() ).build();
		service = restAdapter.create(PushBulletService.class);
	}

	static class SingletonHolder {
		static PushBullet instance = new PushBullet();
	}

	public static PushBullet getInstance() {
		return SingletonHolder.instance;
	}

	public List<PushBulletDevice> getDevices() {
		if (StringUtils.isNoneEmpty( accessToken )) {
			return service.getDevices("Bearer " + accessToken).getDevices();
		}
		return new ArrayList<>();
	}
	
	public void pushNote( String title, String body ) {
		service.push("Bearer " + accessToken, deviceIdent, "note", title, body, null);
	}

	public void pushLink( String title, String body, String url ) {
		service.push("Bearer " + accessToken, deviceIdent, "link", title, body, url);
	}

}
