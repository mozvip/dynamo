package com.github.dynamo.backlog.tasks.core;

import com.github.dynamo.core.model.DownloadableTask;
import com.github.dynamo.model.Downloadable;

public abstract class FindDownloadableImageTask<T extends Downloadable> extends DownloadableTask<T> {
	
	public FindDownloadableImageTask( T downloadable ) {
		super(downloadable);
	}

}
