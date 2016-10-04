package dynamo.backlog.tasks.core;

import java.nio.file.Path;

import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.model.Downloadable;

public abstract class FindDownloadableImageExecutor<T extends Downloadable> extends TaskExecutor<FindDownloadableImageTask<T>> {

	public FindDownloadableImageExecutor(FindDownloadableImageTask<T> task) {
		super(task);	
	}
	
	public abstract boolean downloadImageTo( Path localImage );
	
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
