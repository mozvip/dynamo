package com.github.dynamo.backlog.tasks.files;

import com.github.dynamo.core.model.DownloadableTask;
import com.github.dynamo.model.Downloadable;

public abstract class RenameDownloadableFilesTask<T extends Downloadable> extends DownloadableTask<T> {

	public RenameDownloadableFilesTask(T downloadable) {
		super(downloadable);
	}
	
	@Override
	public String toString() {
		return String.format("Rename files for %s", downloadable.toString());
	}

}
