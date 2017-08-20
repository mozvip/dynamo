package com.github.dynamo.core.tasks;

import com.github.dynamo.core.model.TaskExecutor;

public class InvokeMethodExecutor extends TaskExecutor<InvokeMethodTask> {

	public InvokeMethodExecutor(InvokeMethodTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		
		task.getMethod().invoke( task.getObject(), task.getParams() );
		
	}

}
