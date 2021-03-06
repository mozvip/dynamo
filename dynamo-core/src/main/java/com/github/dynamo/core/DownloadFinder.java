package com.github.dynamo.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.NotAlwaysReady;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.RegExpMatcher;

public abstract class DownloadFinder implements Reconfigurable, Enableable, NotAlwaysReady {
	
	protected final static Logger logger = LoggerFactory.getLogger( DownloadFinder.class );
	
	private Semaphore semaphore = new Semaphore( 2 );

	private boolean ready = false;
	
	private final static String megsExpression = "\\s*([\\d\\.,]+)\\D*[Mm][OoBb]?\\s*.*";
	private final static String gigsExpression = "\\s*([\\d\\.,]+)\\D*[Gg][OoBb]?\\s*.*";

	@Configurable
	private boolean enabled;
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}
	
	public void release() {
		semaphore.release();
	}
	
	public String getCleanName( String name ) {
		name = name.replaceAll("\\W", " ").trim();
		return name.replaceAll("\\s+", " ").trim();
	}
	
	public abstract boolean needsLanguageInSearchString();

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

	public String searchString(String value) throws UnsupportedEncodingException {
		
		value = value.replaceAll("[\\(\\)\\[\\]/!-]", " ");
		value = value.replaceAll("\\s+", " ");
		
		return URLEncoder.encode(value, "UTF-8");
	}
	
	@Override
	public boolean isReady() {
		return ready;
	}
	
	public static float parseSize(String sizeExpression) {
		if (RegExpMatcher.matches(sizeExpression, megsExpression)) {
			return Float.parseFloat( RegExpMatcher.groups(sizeExpression, megsExpression).get(0).replaceAll(",", ".") );				
		}
		if (RegExpMatcher.matches(sizeExpression, gigsExpression)) {
			return Float.parseFloat( RegExpMatcher.groups(sizeExpression, gigsExpression).get(0).replaceAll(",", ".") ) * 1024;				
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
					String label = DynamoObjectFactory.getClassDescription( this.getClass() );
					ErrorManager.getInstance().reportThrowable(String.format("Configuration of %s failed : %s", label, e.getMessage()), e);
					setEnabled(false);
				}
				ready = true;
			}
		} else {
			ready = true;
		}
	}
	
	public abstract void configureProvider() throws Exception;

}
