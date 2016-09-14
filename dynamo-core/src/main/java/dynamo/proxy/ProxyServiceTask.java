
package dynamo.proxy;

import dynamo.backlog.queues.ServiceQueue;
import dynamo.core.DynamoTask;
import dynamo.core.configuration.Configurable;
import dynamo.core.model.ServiceTask;

@DynamoTask(queueClass=ServiceQueue.class)
public class ProxyServiceTask extends ServiceTask {
	
	@Configurable(category="Main Settings")
	private boolean enabled;
	
	@Configurable(category="Main Settings", defaultValue="3128")
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
