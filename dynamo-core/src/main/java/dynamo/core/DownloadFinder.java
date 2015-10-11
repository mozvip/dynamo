package dynamo.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.NotAlwaysReady;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ErrorManager;
import hclient.HTTPClient;
import hclient.RegExpMatcher;

public abstract class DownloadFinder implements Reconfigurable, Enableable, NotAlwaysReady {
	
	protected final static Logger logger = LoggerFactory.getLogger( DownloadFinder.class );

	private boolean ready = false;
	
	private final static String megsExpression = "\\s*([\\d\\.,]+)\\D+[Mm][OoBb]?\\s*.*";
	private final static String gigsExpression = "\\s*([\\d\\.,]+)\\D+[Gg][OoBb]?\\s*.*";

	@Configurable(category="Providers")
	private boolean enabled;
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	protected HTTPClient client = HTTPClient.getInstance();

	public Path download( String url, String referer ) throws IOException {
		if (isEnabled()) {
			while (!isReady()) {
				try {
					Thread.sleep( 1000 );
				} catch (InterruptedException e) {
				}
			}
			return client.download( url, referer );
		}
		return null;
	}

	public String plus(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}
	
	@Override
	public boolean isReady() {
		return ready;
	}
	
	protected float parseSize(String sizeExpression) {
		if (RegExpMatcher.matches(sizeExpression, megsExpression)) {
			return Float.parseFloat( RegExpMatcher.groups(sizeExpression, megsExpression).get(0).replaceAll(",", "") );				
		}
		if (RegExpMatcher.matches(sizeExpression, gigsExpression)) {
			return Float.parseFloat( RegExpMatcher.groups(sizeExpression, gigsExpression).get(0).replaceAll(",", "") ) * 1024;				
		}
		return 0;
	}

	
	@Override
	public void reconfigure() {
		if (isEnabled()) {
			synchronized (this) {
				ready = false;
				try {
					configureProvider();
				} catch (Exception e) {
					ErrorManager.getInstance().reportThrowable(e);
					setEnabled(false);
				}
				ready = true;
			}
		} else {
			ready = true;
		}
	}
	
	public abstract void configureProvider() throws Exception;
	
	public abstract String getLabel();
	
	@Override
	public String toString() {
		return getLabel();
	}

}
