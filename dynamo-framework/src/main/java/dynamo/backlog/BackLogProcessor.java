package dynamo.backlog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import dynamo.backlog.tasks.core.ImmediateTask;
import dynamo.core.Enableable;
import dynamo.core.EventManager;
import dynamo.core.LogQueuing;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DaemonTask;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;
import dynamo.scripting.JavaScriptManager;

public class BackLogProcessor extends Thread {

	private boolean shutdownRequested = false;

	private Set<Class> blackListedTaskClass = new HashSet<>();
	private ExecutorService pool = Executors.newFixedThreadPool(15);
	private TaskSubmission nextInLine = null;

	private List<TaskSubmission> submissions = new ArrayList<>();
	private List<TaskSubmission> submissionsToDisplay = new ArrayList<>();

	private class SubmissionSpecs {
		Class<? extends Task> taskClass;
		long submissionId;
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
						submissions.stream()
							.filter( s -> s != null)
							.filter( s -> match( s, specs))
							.forEach( s -> cancel( s ) );
					}
					
					submissions.removeAll(
						submissions.stream()
							.filter( s -> !(s.getTask() instanceof DaemonTask))
							.filter( s -> s.getFuture() != null && (s.getFuture().isDone() || s.getFuture().isCancelled()))
							.collect(Collectors.toList())
					);
					
					// copy for UI
					submissionsToDisplay = new ArrayList<>();
					submissionsToDisplay.addAll( submissions );
					
					LocalDateTime now = LocalDateTime.now();
					
					if (nextInLine == null) {
						
						Optional<TaskSubmission> selectedSubmission = submissions.stream()
							.filter( s -> s.getFuture() == null || s.getFuture().isDone())
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
		
		if (task instanceof ImmediateTask) {
			runImmediately(task, reportQueued);
			return null;
		} 
		
		TaskSubmission submission = null;

		synchronized (submissions) {

			for (Iterator<TaskSubmission> iterator = submissions.iterator(); iterator.hasNext();) {
				TaskSubmission s = iterator.next();
				if (s.getTask().equals( task )) {
					iterator.remove();
				}
			}
			
			Class<? extends TaskExecutor> backLogTaskClass = ConfigurationManager.getInstance().getActivePlugin( task.getClass() );
			if (backLogTaskClass != null) {
				TaskExecutor<Task> executor = ConfigurationManager.getInstance().newExecutorInstance( backLogTaskClass, task );
				submission = new TaskSubmission(task, executor, minDate );
				submissions.add( submission );
	
				if (task instanceof LogQueuing) {
					ErrorManager.getInstance().reportDebug(task, String.format("%s was queued", task.toString()));
				}
				
				if (nextInLine == null && (minDate == null || minDate.isBefore( LocalDateTime.now() ))) {
					nextInLine = submission;
				}
	
				if (reportQueued) {
					EventManager.getInstance().reportInfo( String.format("%s has been queued", task.toString()));
				}
			} else {
				ErrorManager.getInstance().reportWarning( String.format( "No Executor capable of executing '%s' was found", task ));				
				blackListedTaskClass.add( task.getClass() );
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
		boolean match = false;
		Task task = submission.getTask();
		if (specs.taskClass != null) {
			match = specs.taskClass.isAssignableFrom( task.getClass() );
		}
		if (specs.expressionToVerify != null) {
			try {
				match = evaluate( task, specs.expressionToVerify );
			} catch (ScriptException e) {
			}
		}
		if (specs.submissionId >= 0 ) {
			match = specs.submissionId == submission.getSubmissionId();
		}
		
		return match;
	}

	public boolean isRunningOrPending( SubmissionSpecs specs ) {
		synchronized (submissions) {
			return submissions.stream().filter( submission -> match( submission, specs )).findAny().isPresent();
		}
	}

	public void unschedule( Class<? extends Task> taskClass, String expressionToVerify ) {
		toUnschedule.add( new SubmissionSpecs( taskClass, expressionToVerify ) );
	}

	public void unschedule( long submissionId ) {
		toUnschedule.add( new SubmissionSpecs( submissionId ) );
	}

	public void runImmediately(Task task, boolean reportQueued) {
		Class<? extends TaskExecutor> backLogTaskClass = ConfigurationManager.getInstance().getActivePlugin( task.getClass() );
		if (backLogTaskClass != null) {
			TaskExecutor<Task> executor = ConfigurationManager.getInstance().newExecutorInstance( backLogTaskClass, task );
			try {
				executor.execute();
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
	}

	private void cancel( TaskSubmission submission ) {
		submission.getExecutor().cancel();
	}

	public boolean isRunningOrPending(Class<? extends Task> taskClass) {
		SubmissionSpecs specs = new SubmissionSpecs(taskClass, null);
		return isRunningOrPending(specs);
	}

}
