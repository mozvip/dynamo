
package dynamo.proxy;

import dynamo.core.configuration.Configurable;
import dynamo.core.model.ServiceTask;

public class ProxyServiceTask extends ServiceTask {
	
	@Configurable
	private boolean enabled;
	
	@Configurable(ifExpression="ProxyServiceTask.enabled", defaultValue="3128", required=true)
	private int port = 3128;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return "LittleProxy Service";
	}

}
