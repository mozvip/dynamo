package dynamo.backlog.tasks.files;

import dynamo.core.model.DownloadableTask;
import dynamo.model.Downloadable;

public abstract class FileOperationTask extends DownloadableTask {

	public FileOperationTask(Downloadable downloadable) {
		super(downloadable);
	}

}
