package dynamo.core.model;

import dynamo.model.Downloadable;

public abstract class DownloadableTask extends Task {
	
	public DownloadableTask( Downloadable downloadable ) {
		this.downloadable = downloadable;
	}
	
	protected Downloadable downloadable;

	public Downloadable getDownloadable() {
		return downloadable;
	}

}
