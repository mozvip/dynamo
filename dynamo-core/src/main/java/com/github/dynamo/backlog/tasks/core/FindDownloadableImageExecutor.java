package com.github.dynamo.backlog.tasks.core;

import java.io.IOException;
import java.nio.file.Path;

import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.Downloadable;

public abstract class FindDownloadableImageExecutor<T extends Downloadable> extends TaskExecutor<FindDownloadableImageTask<T>> {

	public FindDownloadableImageExecutor(FindDownloadableImageTask<T> task) {
		super(task);	
	}
	
	public abstract boolean downloadImageTo( Path localImage ) throws IOException;
	
	public void onImageFound( Path localImage ) {
		
	}

	@Override
	public void execute() throws Exception {
		
		Path localImage = DownloadableManager.resolveImage( task.getDownloadable() );
		
		boolean result = downloadImageTo(localImage);

		if (result) {
			onImageFound(localImage);
		}

	}
}
