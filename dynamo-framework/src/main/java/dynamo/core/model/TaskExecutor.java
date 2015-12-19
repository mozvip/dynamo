package dynamo.core.model;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.EventManager;
import dynamo.core.manager.ErrorManager;
import hclient.HTTPClient;

public abstract class TaskExecutor<T extends Task> implements Runnable {

	protected final static Logger LOGGER = LoggerFactory.getLogger( TaskExecutor.class );

	private boolean running = false;
	private boolean done = false;
	private boolean failed = false;

	protected T task = null;
	protected boolean cancelled = false;
	
	protected HTTPClient client = HTTPClient.getInstance();
	
	private String currentLabel;
	
	public TaskExecutor( T task ) {
		this.task = task;
		this.currentLabel = task.toString();
	}

	public T getTask() {
		return task;
	}
	
	public String getCurrentLabel() {
		return currentLabel;
	}
	
	public void setCurrentLabel(String currentLabel) {
		this.currentLabel = currentLabel;
	}
	
	public void cancel() {
		cancelled = true;
		shutdown();
	}
	
	public void shutdown() {
		// default implementation does nothing
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean isReportProgress() {
		return this instanceof ReportProgress;
	}
	
	public int getItemsPercent() {
		if (isReportProgress()) {
			ReportProgress p = (ReportProgress) this;
			return p.getTotalItems() == 0 ? 0 : (int)(p.getItemsDone() / (float) p.getTotalItems() * 100.0f);		
		}
		
		return isDone() ? 100 : 0;
	}
	
	@Override
	protected void finalize() throws Throwable {
		shutdown();
		super.finalize();
	}

	@Override
	public void run() {
		running = true;
		
		long startTime = System.currentTimeMillis();
		
		Exception eTask = null;

		try {
			
			if ( task instanceof ServiceTask ) {
				execute();
				while ( !cancelled ) {
					Thread.sleep( 5000 );
				}
			} else {
				execute();
			}
		} catch (Exception e) {
			eTask = e;
			failed = true;
		} finally {

			shutdown();

			long endTime = System.currentTimeMillis();
			
			float timeTaken = (endTime - startTime) / 1000.0f;

			done = true;
			running = false;

			if (!(task instanceof NoLogging)) {
				if (failed) {
					String messageToReport = eTask != null ? eTask.getMessage() : null;
					if (messageToReport == null && eTask != null) {
						messageToReport	= eTask.toString();
					}
					String label = null;
					if (messageToReport == null) {
						label = String.format("Task failed (after %8.2f secs)", timeTaken);
					} else {
						label = String.format("Task failed (after %8.2f secs) : %s", timeTaken, messageToReport);
					}
					ErrorManager.getInstance().reportThrowable( task, label, eTask );
					if ( this instanceof ReportFinished ) {
						EventManager.getInstance().reportError( String.format( "Task : %s failed !", task.toString() ) );
					}
				} else {
					if ( this instanceof LogSuccess ) {
						ErrorManager.getInstance().reportDebug( task, String.format("Task successful (in %8.2f secs)", timeTaken) );
					}
					if ( this instanceof  ReportFinished ) {
						EventManager.getInstance().reportSuccess( String.format( "Task : %s is finished", task.toString() ) );
					}
				}
			}

			try {
				rescheduleTask( task );
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( task, e );
				failed = true;
			}
		}

	}
	
	public void rescheduleTask( T taskToReschedule ) {
	}

	public boolean isDone() {
		return done;
	}
	
	public boolean isFailed() {
		return failed;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public boolean isFinished() {
		return isDone() || isFailed() || isCancelled();
	}

	public Date getNextDate( int minutes ) {
		Calendar calendar = Calendar.getInstance();
		calendar.add( Calendar.MINUTE, minutes );
		return calendar.getTime();
	}

	public abstract void execute() throws Exception; 
	
	@Override
	public int hashCode() {
		return getTask().toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals( this.getClass() )) {
			return ((TaskExecutor<?>)obj).getTask().equals( this.getTask() );
		}
		return false;
	}

	protected void queue( Task task ) {
		queue(task, true );
	}	

	protected void queue( Task task, boolean reportQueued ) {
		BackLogProcessor.getInstance().schedule(task, reportQueued );
	}	

	protected void runSync( Task task ) throws Exception {
		BackLogProcessor.getInstance().runSync( task, false );
	}
	
	public String getDescription() {
		return getClass().getName();
	}

}
