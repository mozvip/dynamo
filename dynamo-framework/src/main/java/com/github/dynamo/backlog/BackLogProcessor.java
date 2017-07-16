package com.github.dynamo.backlog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dynamo.core.Enableable;
import com.github.dynamo.core.EventManager;
import com.github.dynamo.core.LogQueuing;
import com.github.dynamo.core.manager.ConfigurationManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.DaemonTask;
import com.github.dynamo.core.model.Task;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.scripting.JavaScriptManager;
import com.google.common.eventbus.EventBus;

public class BackLogProcessor extends Thread {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BackLogProcessor.class);

	private boolean shutdownRequested = false;

	private Set<Class> blackListedTaskClass = new HashSet<>();
	private ExecutorService pool = Executors.newFixedThreadPool(15);
	private TaskSubmission nextInLine = null;
	private EventBus eventBus;

	private Map<Long, TaskSubmission> submissions = new HashMap<>();
	private List<TaskSubmission> submissionsToDisplay = new ArrayList<>();

	private class SubmissionSpecs {
		Class<? extends Task> taskClass;
		long submissionId = -1;
		String expressionToVerify;
		public SubmissionSpecs(Class<? extends Task> taskClass, String expressionToVerify) {
			super();
			this.taskClass = taskClass;
			this.expressionToVerify = expressionToVerify;
		}
		public SubmissionSpecs(long submissionId) {
			this.submissionId = submissionId;
		}
	}
	
	private LinkedList<SubmissionSpecs> toUnschedule = new LinkedList<>();
	private Set<Long> toRunNow = new HashSet<>();

	private BackLogProcessor() {
		setDaemon( true );
	}

	static class SingletonHolder {
		static BackLogProcessor instance = new BackLogProcessor();
	}

	public static BackLogProcessor getInstance() {
		return SingletonHolder.instance;
	}
	
	public Collection<TaskSubmission> getSubmissions() {
		return submissionsToDisplay;
	}
	
	public Collection<TaskSubmission> getSubmissions( String queryExpression ) {
		return submissionsToDisplay.stream().filter( s -> {
			try {
				return evaluate(s.getTask(), queryExpression);
			} catch (ScriptException e) {
				LOGGER.error(e.getMessage(), e);
				return false;
			}
		} ).collect(Collectors.toList());
	}	
	
	@Override
	public void run() {

		try {
			for (;;) {
				
				if (shutdownRequested) {
					break;
				}
				
				TaskSubmission submission = null;

				synchronized (submissions) {
					
					// cancel requested
					while (toUnschedule.peek() != null) {
						SubmissionSpecs specs = toUnschedule.pop();
						submissions.values().stream()
							.filter( s -> s != null)
							.filter( s -> match( s, specs))
							.forEach( s -> cancel( s ) );
					}
					
					List<Long> deadSubmissionIds = submissions.values().stream()
						.filter( s -> !(s.getTask() instanceof DaemonTask))
						.filter( s -> s.getFuture() != null && (s.getFuture().isDone() || s.getFuture().isCancelled()) )
						.map( s -> s.getSubmissionId() )
						.collect(Collectors.toList());
					
					submissions.values().stream()
						.filter( s -> s.getTask() instanceof DaemonTask)
						.forEach( s -> s.setFuture( null ) );

					for (Long submissionId : deadSubmissionIds) {
						submissions.remove( submissionId );
					}
					
					synchronized (toRunNow) {
						for (Long submissionId : toRunNow) {
							submissions.get(submissionId).setMinDate( null );
						}
						toRunNow.clear();
					}

					// copy for UI
					submissionsToDisplay = new ArrayList<>( submissions.values() );
					
					LocalDateTime now = LocalDateTime.now();
					
					if (nextInLine == null) {
						
						Optional<TaskSubmission> selectedSubmission = submissions.values().stream()
							.filter( s -> s.getFuture() == null)
							.filter( s -> s.getMinDate() == null || s.getMinDate().isBefore( now ) )
							.findFirst();					
						submission = selectedSubmission.isPresent() ? selectedSubmission.get() : null;

					} else {

						submission = nextInLine;
						nextInLine = null;

					}
				}

				if (submission == null) {
					Thread.sleep( 1000 );
					continue;
				}

				Task task = submission.getTask();

				if (task instanceof DaemonTask) {
					DaemonTask daemon = (DaemonTask) task;
					submission.setMinDate( LocalDateTime.now().plusMinutes( daemon.getMinutesFrequency() ) );
				}

				submission.setFuture( pool.submit( submission.getExecutor() ) );

			}
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		} finally {
			pool.shutdownNow();
		}
	}

	public TaskSubmission schedule( Task task ) {
		return schedule( task, null, true );
	}

	public TaskSubmission schedule( Task task, LocalDateTime minDate ) {
		return schedule( task, minDate, true );
	}

	public TaskSubmission schedule( Task task, boolean reportQueued ) {
		return schedule( task, null, reportQueued );
	}

	public TaskSubmission schedule( Task task, LocalDateTime minDate, boolean reportQueued ) {
		if (task instanceof Enableable && !((Enableable) task).isEnabled()) {
			return null;
		}
		
		Class<? extends TaskExecutor> backLogTaskClass = ConfigurationManager.getInstance().getActivePlugin( task.getClass() );
		if (backLogTaskClass == null) {
			ErrorManager.getInstance().reportWarning( String.format( "No Executor capable of executing '%s' was found", task ));				
			blackListedTaskClass.add( task.getClass() );
			return null;
		}		
	
		TaskSubmission submission = null;

		synchronized (submissions) {

			for (Iterator<TaskSubmission> iterator = submissions.values().iterator(); iterator.hasNext();) {
				TaskSubmission s = iterator.next();
				if (s.getTask().equals( task )) {
					iterator.remove();
					break;
				}
			}

			TaskExecutor<Task> executor = ConfigurationManager.getInstance().newExecutorInstance( backLogTaskClass, task );
			submission = new TaskSubmission(task, executor, minDate );
			submissions.put( submission.getSubmissionId(), submission );

			if (task instanceof LogQueuing) {
				ErrorManager.getInstance().reportDebug(task, String.format("%s was queued", task.toString()));
			}
			
			if (nextInLine == null && (minDate == null || minDate.isBefore( LocalDateTime.now() ))) {
				nextInLine = submission;
			}

			if (reportQueued) {
				EventManager.getInstance().reportInfo( String.format("%s has been queued", task.toString()));
			}
			
		}
		
		return submission;
	}

	public void shutdown() {
		shutdownRequested = true;
	}

	public void unschedule( Class<? extends Task> taskClass ) {
		unschedule(taskClass, null);
	}

	public void unschedule( String expressionToVerify ) {
		unschedule(null, expressionToVerify);
	}

	private boolean evaluate( Task task, String expressionToVerify ) throws ScriptException {
		boolean result = false;
		if (expressionToVerify != null) {
			synchronized ( this ) {
				JavaScriptManager.getInstance().put("task", task);
				result = (Boolean) JavaScriptManager.getInstance().eval( expressionToVerify );
			}
		} else {
			result = true;
		}
		return result;
	}

	private boolean match( TaskSubmission submission, SubmissionSpecs specs ) {
		Task task = submission.getTask();
		if (specs.taskClass != null) {
			if (!specs.taskClass.isAssignableFrom( task.getClass() )) {
				return false;
			}
		}
		if (specs.submissionId >= 0 ) {
			if( specs.submissionId != submission.getSubmissionId()) {
				return false;
			}
		}
		if (specs.expressionToVerify != null) {
			try {
				return evaluate( task, specs.expressionToVerify );
			} catch (ScriptException e) {
			}
		}
		return false;
	}

	public boolean isRunningOrPending( SubmissionSpecs specs ) {
		synchronized (submissions) {
			return submissions.values().stream().filter( submission -> match( submission, specs )).findAny().isPresent();
		}
	}

	public void unschedule( Class<? extends Task> taskClass, String expressionToVerify ) {
		toUnschedule.add( new SubmissionSpecs( taskClass, expressionToVerify ) );
	}

	public void unschedule( long submissionId ) {
		toUnschedule.add( new SubmissionSpecs( submissionId ) );
	}

	private void cancel( TaskSubmission submission ) {
		submission.getExecutor().cancel();
	}

	public boolean isRunningOrPending(Class<? extends Task> taskClass) {
		SubmissionSpecs specs = new SubmissionSpecs(taskClass, null);
		return isRunningOrPending(specs);
	}

	public void runNow(long submissionId) {
		synchronized (toRunNow) {
			toRunNow.add( submissionId );
		}
	}

	public void post(Object event) {
		eventBus.post( event );
	}

	public EventBus newEventBus() {
		eventBus = new EventBus();
		return eventBus;
	}
	
}
