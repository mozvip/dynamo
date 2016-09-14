package dynamo.core;

import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ErrorManager;
import hclient.HTTPClient;


public abstract class SubtitlesFinder implements Reconfigurable, Enableable {
	
	@Configurable(defaultValue="true")
	private boolean enabled;
	
	private boolean ready;
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	protected HTTPClient client = HTTPClient.getInstance();
	
	@Override
	public void reconfigure() {
		this.ready = false;
		try {
			try {
				customInit();
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( e );
				this.enabled = false;
			}
		} finally {
			this.ready = true;
		}
	}

	public void customInit() throws Exception {
	}
	
	public void warn( String message ) {
		ErrorManager.getInstance().reportWarning( String.format("%s : %s", getClass().getSimpleName(), message ));
	}
	
	public abstract FinderQuality getQuality();
	
	public boolean isReady() {
		return ready;
	}
	
	protected abstract RemoteSubTitles downloadSubtitle( VideoDetails details , Language language) throws Exception;

	public RemoteSubTitles findSubtitles( VideoDetails details , Language language) throws Exception {
		while (!isReady()) {
			// currently being reconfigured
			Thread.sleep( 500 );
		}
		return downloadSubtitle( details, language );
	}

}
