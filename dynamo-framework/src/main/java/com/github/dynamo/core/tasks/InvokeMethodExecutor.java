package com.github.dynamo.core.tasks;

import com.github.dynamo.core.model.TaskExecutor;

public class MethodInvokeExecutor extends TaskExecutor<InvokeMethodTask> {

	public MethodInvokeExecutor(InvokeMethodTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		
		task.getMethod().invoke( task.getObject(), task.getParams() );
		
	}

}
