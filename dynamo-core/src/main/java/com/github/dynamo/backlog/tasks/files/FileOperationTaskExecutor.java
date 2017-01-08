package com.github.dynamo.backlog.tasks.files;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.model.LogSuccess;
import com.github.dynamo.core.model.TaskExecutor;

public abstract class FileOperationTaskExecutor<T extends FileOperationTask> extends TaskExecutor<FileOperationTask> implements LogSuccess {
	
	public FileOperationTaskExecutor(T task) {
		super(task);
	}

	public abstract boolean isFinished();

	@Override
	public void rescheduleTask( FileOperationTask item ) {
		if (!isFinished()) {
			// we will try again in 5 minutes
			BackLogProcessor.getInstance().schedule( item, getNextDate( 5 ), false );
		}
	}

}
