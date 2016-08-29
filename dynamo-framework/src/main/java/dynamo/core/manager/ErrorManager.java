package dynamo.core.manager;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.EventManager;
import dynamo.core.logging.LogDAO;
import dynamo.core.logging.LogItem;
import dynamo.core.logging.LogItemSeverity;
import dynamo.core.model.Task;

public class ErrorManager {
	
	private final static Logger logger = LoggerFactory.getLogger( ErrorManager.class );
	
	private LogDAO logDAO;

	private ErrorManager() {
		logDAO = DAOManager.getInstance().getDAO( LogDAO.class );
	}

	static class SingletonHolder {
		static ErrorManager instance = new ErrorManager();
	}
	
	public static ErrorManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public void reportThrowable( Task task, Throwable t ) {
		reportThrowable( task, null, t );
	}
	
	public void reportThrowable( String message, Throwable t ) {
		reportThrowable( null, message, t );
	}

	public void logThrowable( Throwable t ) {
		String messageToLog = String.format("%s : %s", t.toString(), t.getMessage());
		logThrowable(messageToLog, null, t);
	}
	
	public void logThrowable( String messageToLog, Task task, Throwable t ) {
		BackLogProcessor.getInstance().schedule( new LogTask(messageToLog, LogItemSeverity.ERROR, task, t, null), false);
	}

	public void reportThrowable( Task task, String message, Throwable t ) {
		String messageToLog = message;
		if (message == null && t != null) {
			messageToLog = String.format("%s : %s", t.toString(), t.getMessage());
		}
		logger.error( messageToLog, t);

		logThrowable(messageToLog, task, t);
		if (task != null) {
			EventManager.getInstance().reportError(String.format("%s failed", task.toString()), messageToLog);
		} else {
			EventManager.getInstance().reportError(messageToLog);
		}
	}
	
	public void reportWarning( Task task, String message, boolean logOnly ) {
		BackLogProcessor.getInstance().schedule( new LogTask(message, LogItemSeverity.WARNING, task, null, Thread.currentThread().getStackTrace()), false);
		if (!logOnly) {
			EventManager.getInstance().reportWarning(message);
		}
	}

	public void reportWarning( String message ) {
		reportWarning( null, message, true );
	}

	public void reportWarning( String message, boolean logOnly ) {
		reportWarning( null, message, logOnly );
	}

	public void reportError( Task task, String message ) {
		BackLogProcessor.getInstance().schedule( new LogTask(message, LogItemSeverity.ERROR, task, null, Thread.currentThread().getStackTrace()), false);
		EventManager.getInstance().reportError(message);
	}

	public void reportError( String message ) {
		reportError(null, message);
	}

	public void reportInfo(String message) {
		reportInfo(null, message);
	}

	public void reportThrowable( Throwable t ) {
		reportThrowable( (Task) null, t);
	}

	public List<LogItem> findLogItems(List<LogItemSeverity> filterSeverities) {
		return logDAO.findLogItems( StringUtils.join( filterSeverities, ',' ) );
	}
	
	public void reportInfo( Task task, String message ) {
		BackLogProcessor.getInstance().schedule( new LogTask(message, LogItemSeverity.INFO, task, null, Thread.currentThread().getStackTrace()), false);
		EventManager.getInstance().reportInfo(message);
	}
	
	public void reportDebug( Task task, String message ) {
		BackLogProcessor.getInstance().schedule( new LogTask(message, LogItemSeverity.DEBUG, task, null, Thread.currentThread().getStackTrace()), false);
	}

	public List<StackTraceElement> getStackTrace(long id) {
		return logDAO.getStackTrace(id);
	}

}
