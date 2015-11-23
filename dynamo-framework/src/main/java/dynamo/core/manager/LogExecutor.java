package dynamo.core.manager;

import dynamo.core.logging.LogDAO;
import dynamo.core.model.TaskExecutor;

public class LogExecutor extends TaskExecutor<LogTask> {
	
	private LogDAO logDAO;

	public LogExecutor(LogTask task, LogDAO logDAO) {
		super(task);
		this.logDAO = logDAO;
	}

	@Override
	public void execute() throws Exception {
		long logItemId = logDAO.create( task.getMessage(), task.getSeverity(), task.getTask() != null ? task.getTask().toString() : null);
		if (task.getStackTrace() != null) {
			logDAO.insertStackTrace( task.getStackTrace(), logItemId );
		}
	}

}
