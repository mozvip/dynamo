package com.github.dynamo.backlog.tasks.files;

import com.github.dynamo.core.model.DownloadableTask;
import com.github.dynamo.model.Downloadable;

public abstract class RenameDownloadableFilesTask<T extends Downloadable> extends DownloadableTask<T> {

	public RenameDownloadableFilesTask(T downloadable) {
		super(downloadable);
	}

}
