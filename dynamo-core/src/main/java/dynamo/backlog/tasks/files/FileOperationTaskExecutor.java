package dynamo.backlog.tasks.files;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.LogSuccess;
import dynamo.core.model.TaskExecutor;

public abstract class FileOperationTaskExecutor<T extends FileOperationBackLogItem> extends TaskExecutor<FileOperationBackLogItem> implements LogSuccess {
	
	public FileOperationTaskExecutor(T task) {
		super(task);
	}

	public abstract boolean isFinished();

	@Override
	public void rescheduleTask( FileOperationBackLogItem item ) {
		if (!isFinished()) {
			// we will try again in 5 minutes
			item.setMinDate( getNextDate( 5 ) );
			BackLogProcessor.getInstance().schedule( item, false );
		}
	}

}
