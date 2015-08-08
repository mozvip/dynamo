package dynamo.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.http.client.ClientProtocolException;

import dynamo.trakt.TraktManager;

@ManagedBean(name="trakt")
@ViewScoped
public class Trakt {
	
	private String clientId = TraktManager.clientId;
	private String userName = TraktManager.getInstance().getUsername();
	private String oauth;
	
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getOauth() {
		return oauth;
	}

	public void setOauth(String oauth) {
		this.oauth = oauth;
	}

	public void save() throws ClientProtocolException, UnsupportedEncodingException, IOException {
		TraktManager.getInstance().setUsername(userName);
		TraktManager.getInstance().auth( oauth );
	}

}
