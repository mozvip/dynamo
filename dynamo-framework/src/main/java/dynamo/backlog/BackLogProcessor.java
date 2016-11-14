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

import javax.el.ExpressionFactory;

import dynamo.core.Enableable;
import dynamo.core.EventManager;
import dynamo.core.LogQueuing;
import dynamo.core.el.DynamoELContext;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DaemonTask;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;

public class BackLogProcessor extends Thread {

	private boolean shutdownRequested = false;

	private Set<Class> blackListedTaskClass = new HashSet<>();
	private ExecutorService pool = Executors.newFixedThreadPool(15);
	private TaskSubmission nextInLine = null;

	private List<TaskSubmission> submissions = new ArrayList<>();

	private class UnscheduleSpecs {
		Class<? extends Task> taskClass;
		String expressionToVerify;
		public UnscheduleSpecs(Class<? extends Task> taskClass, String expressionToVerify) {
			super();
			this.taskClass = taskClass;
			this.expressionToVerify = expressionToVerify;
		}
	}
	
	private LinkedList<UnscheduleSpecs> toUnschedule = new LinkedList<>();

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
		return submissions;
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
					while (!toUnschedule.isEmpty()) {
						UnscheduleSpecs specs = toUnschedule.pop();
						submissions.stream()
							.filter( s -> s != null)
							.filter( s -> match( s.getTask(), specs.taskClass, specs.expressionToVerify)).forEach( s -> cancel( s.getSubmissionId() ) );
					}
					
					submissions.removeAll(
						submissions.stream()
							.filter( s -> !(s.getTask() instanceof DaemonTask))
							.filter( s -> s.getFuture() != null && (s.getFuture().isDone() || s.getFuture().isCancelled()))
							.collect(Collectors.toList())
					);
					
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

	private boolean match( Task task, Class<? extends Task> taskClass, String expressionToVerify ) {

		boolean result = false;

		if (taskClass == null || taskClass.isAssignableFrom( task.getClass() )) {
			if (expressionToVerify != null) {
				try {
					DynamoELContext context = new DynamoELContext( task );
					result = (Boolean) ExpressionFactory.newInstance().createValueExpression( context, "#{" + expressionToVerify + "}", Boolean.class ).getValue( context );
				} catch (javax.el.PropertyNotFoundException e) {
					if (taskClass != null) {
						throw e;
					} else {
						// this is expected to happen
					}
				}
			} else {
				result = true;
			}
		}
		
		return result;
		
	}

	public boolean isRunningOrPending( Class<? extends Task> taskClass ) {
		return submissions.stream().filter( submission -> match( submission.getTask(), taskClass, null )).findAny().isPresent();
	}

	public void unschedule( Class<? extends Task> taskClass, String expressionToVerify ) {
		toUnschedule.add( new UnscheduleSpecs( taskClass, expressionToVerify ) );
	}

	public void runSync(Task task, boolean reportQueued) throws Exception {
		Class<? extends TaskExecutor> backLogTaskClass = ConfigurationManager.getInstance().getActivePlugin( task.getClass() );
		if (backLogTaskClass != null) {
			TaskExecutor<Task> executor = ConfigurationManager.getInstance().newExecutorInstance( backLogTaskClass, task );
			executor.execute();
		}
	}

	public void cancel(long submissionId) {
		synchronized (submissions) {
			for (Iterator<TaskSubmission> iterator = submissions.iterator(); iterator.hasNext();) {
				TaskSubmission submission = iterator.next();
				if (submission.getSubmissionId() == submissionId ) {
					if (submission.getFuture() != null) {
						submission.getFuture().cancel( false );
					}
					submission.getExecutor().cancel();
					iterator.remove();
					break;
				}
			}
		}
	}

}
