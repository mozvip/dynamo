package com.github.dynamo.core.model;

import com.github.dynamo.core.model.Task;
import com.github.dynamo.model.Downloadable;

public abstract class DownloadableTask extends Task {
	
	public DownloadableTask( Downloadable downloadable ) {
		this.downloadable = downloadable;
	}
	
	protected Downloadable downloadable;

	public Downloadable getDownloadable() {
		return downloadable;
	}

}
