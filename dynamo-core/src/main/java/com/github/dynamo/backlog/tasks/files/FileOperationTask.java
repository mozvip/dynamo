package com.github.dynamo.backlog.tasks.files;

import com.github.dynamo.core.model.DownloadableTask;
import com.github.dynamo.model.Downloadable;

public abstract class FileOperationTask extends DownloadableTask {

	public FileOperationTask(Downloadable downloadable) {
		super(downloadable);
	}

}
