package com.github.dynamo.core.model;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dynamo.core.EventManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.mozvip.hclient.HTTPClient;

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
		stop();
	}
	
	public void init() throws Exception {
		// default implementation does nothing
	}

	public void shutdown() throws Exception {
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
		stop();
		super.finalize();
	}

	@Override
	public void run() {
		running = true;
		
		long startTime = System.currentTimeMillis();
		
		Exception eTask = null;
		
		try {

			init();

			if ( task instanceof ServiceTask ) {
				execute();
				while ( !cancelled ) {
					Thread.sleep( 5000 );
				}
			} else {
				execute();
			}
		} catch (InterruptedException e) {
			if (!cancelled) {
				ErrorManager.getInstance().reportThrowable(getTask(), e);	
				eTask = e;
				failed = true;
			}
		} catch (Exception e) {
			eTask = e;
			failed = true;
		} finally {

			stop();

			long endTime = System.currentTimeMillis();
			
			float timeTaken = (endTime - startTime) / 1000.0f;

			done = true;
			running = false;
			
			if (!cancelled) {	// FIXME: log cancelled tasks too

				if (!(task instanceof NoLogging)) {
					if (failed) {
						String messageToReport = eTask != null ? eTask.getMessage() : null;
						if (messageToReport == null && eTask != null) {
							messageToReport	= eTask.toString();
						}
						String label = String.format("Task failed (after %8.2f secs) : %s : %s", timeTaken, eTask.getClass().getName(), messageToReport);
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

	}

	private void stop() {
		try {
			shutdown();
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( task, e );
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

	public LocalDateTime getNextDate( int minutes ) {
		return LocalDateTime.now().plusMinutes( minutes );
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

}
