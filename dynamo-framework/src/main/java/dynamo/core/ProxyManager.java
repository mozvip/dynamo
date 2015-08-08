package dynamo.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;

import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import hclient.HTTPClient;

public class ProxyManager implements Reconfigurable {

	@Configurable(category="Main Settings", name="Use HTTP Proxy for outbound HTTP connections")
	private boolean proxyEnabled;

	@Configurable(category="Main Settings", name="HTTP Proxy Host", disabled="#{!ProxyManager.proxyEnabled}", required="#{ProxyManager.proxyEnabled}")
	private String proxyHost;

	@Configurable(category="Main Settings", name="HTTP Proxy Port", disabled="#{!ProxyManager.proxyEnabled}", required="#{ProxyManager.proxyEnabled}")
	private int proxyPort = 3128;
	
	public boolean isProxyEnabled() {
		return proxyEnabled;
	}

	public void setProxyEnabled(boolean proxyEnabled) {
		this.proxyEnabled = proxyEnabled;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	@Override
	public void reconfigure() {
		if ( proxyEnabled && StringUtils.isNotEmpty( proxyHost ) && proxyPort > 0 ) {
			HTTPClient.getInstance().setProxy( new HttpHost( proxyHost, proxyPort ) );
		} else {
			HTTPClient.getInstance().setProxy( null );
		}
	}

	static class SingletonHolder {
		static ProxyManager instance = new ProxyManager();
	}

	public static ProxyManager getInstance() {
		return SingletonHolder.instance;
	}

}
