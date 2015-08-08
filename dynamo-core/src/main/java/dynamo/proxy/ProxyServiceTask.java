
package dynamo.proxy;

import dynamo.backlog.queues.ServiceQueue;
import dynamo.core.configuration.Configurable;
import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.ServiceTask;

public class ProxyServiceTask extends ServiceTask {
	
	@Configurable(category="Main Settings", name="Enable Local Dynamo Proxy")
	private boolean enabled;
	
	@Configurable(category="Main Settings", name="Listen on port", disabled="#{!ProxyServiceTask.enabled}", required="#{ProxyServiceTask.enabled}", defaultValue="3128")
	private int port = 3128;
	
	@Override
	public Class<? extends AbstractDynamoQueue> getQueueClass() {
		return ServiceQueue.class;
	}

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
