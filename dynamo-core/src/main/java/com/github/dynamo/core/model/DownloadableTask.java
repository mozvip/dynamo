package com.github.dynamo.core.model;

import com.github.dynamo.model.Downloadable;

public abstract class DownloadableTask<T extends Downloadable> extends Task {
	
	public DownloadableTask( T downloadable ) {
		this.downloadable = downloadable;
	}
	
	protected T downloadable;

	public T getDownloadable() {
		return downloadable;
	}

}
