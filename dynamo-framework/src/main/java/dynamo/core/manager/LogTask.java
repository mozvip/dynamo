package dynamo.core.manager;

import dynamo.core.DynamoTask;
import dynamo.core.logging.LogItemSeverity;
import dynamo.core.model.NoLogging;
import dynamo.core.model.Task;

@DynamoTask(queueClass=LoggingQueue.class)
public class LogTask extends Task implements NoLogging {
	
	private String message;
	private LogItemSeverity severity;
	private Task task;
	private Throwable t;
	private StackTraceElement[] stackTrace;

	public LogTask(String message, LogItemSeverity severity, Task task, Throwable t, StackTraceElement[] stackTrace) {
		super();
		this.message = message;
		this.severity = severity;
		this.task = task;
		this.t = t;
		if (t != null) {
			this.stackTrace = t.getStackTrace();
		} else {
			this.stackTrace = stackTrace;
		}
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
	
	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}
	
	public Throwable getT() {
		return t;
	}
	
	@Override
	public String toString() {
		return String.format("Logging message : %s", message);
	}

}
