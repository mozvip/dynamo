package com.github.dynamo.backlog.tasks.core;

import com.github.dynamo.core.model.Task;
import com.github.dynamo.model.Downloadable;

public abstract class FindDownloadableImageTask<T extends Downloadable> extends Task {
	
	private T downloadable;
	
	public FindDownloadableImageTask( T downloadable ) {
		this.downloadable = downloadable;
	}
	
	public T getDownloadable() {
		return downloadable;
	}

}
