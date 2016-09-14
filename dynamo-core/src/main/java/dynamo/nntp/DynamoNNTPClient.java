package dynamo.nntp;

import java.io.IOException;

import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;

import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ErrorManager;

public class DynamoNNTPClient implements Reconfigurable, Enableable {

	@Configurable(category = "NNTP Client")
	private boolean enabled = false;
	@Configurable(category = "NNTP Client")
	private String host;
	@Configurable(category = "NNTP Client")
	private String login;
	@Configurable(category = "NNTP Client")
	private String password;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	private NNTPClient client = null;
	
	private DynamoNNTPClient() {
	}

	static class SingletonHolder {
		static DynamoNNTPClient instance = new DynamoNNTPClient();
	}

	public static DynamoNNTPClient getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public void reconfigure() {
		
		if (enabled && host != null && login != null && password != null) {
			
			if (client != null && client.isConnected()) {
				try {
					client.disconnect();
				} catch (IOException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
			}

			client = new NNTPClient();
			try {
				client.connect( host );
				client.authenticate(login, password);
				

			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
				setEnabled( false );
			}
			
		}
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (client != null) {
			client.disconnect();
		}
		super.finalize();
	}

	public Iterable<String> iterateNewNews(NewGroupsOrNewsQuery query) throws IOException {
		return client.iterateNewNews(query);
	}

}
