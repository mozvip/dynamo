package dynamo.core.manager;

import dynamo.core.logging.LogItemSeverity;
import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.NoLogging;
import dynamo.core.model.Task;

public class LogTask extends Task implements NoLogging {
	
	private String message;
	private LogItemSeverity severity;
	private Task task;
	private Throwable t;
	
	@Override
	public Class<? extends AbstractDynamoQueue> getQueueClass() {
		return LoggingQueue.class;
	}

	public LogTask(String message, LogItemSeverity severity, Task task, Throwable t) {
		super();
		this.message = message;
		this.severity = severity;
		this.task = task;
		this.t = t;
	}

	public String getMessage() {
		return message;
	}
	public LogItemSeverity getSeverity() {
		return severity;
	}
	public Task getTask() {
		return task;
	}
	
	public Throwable getT() {
		return t;
	}
	
	@Override
	public String toString() {
		return String.format("Logging message : %s", message);
	}

}
