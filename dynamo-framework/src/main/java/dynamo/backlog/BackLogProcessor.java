package dynamo.backlog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.el.ExpressionFactory;

import dynamo.core.Enableable;
import dynamo.core.EventManager;
import dynamo.core.LogQueuing;
import dynamo.core.el.DynamoELContext;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.CancellableTask;
import dynamo.core.model.DaemonTask;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;

public class BackLogProcessor extends Thread {

	private boolean shutdownRequested = false;
	
	private BlockingQueue<Task> pendingTasks = new LinkedBlockingQueue<>();
	private List<TaskExecutor> runningExecutors = new ArrayList<>();

	private Set<Class> blackListedTaskClass = new HashSet<>();
	private ExecutorService pool = Executors.newFixedThreadPool(60);
	private List<Future> futures = new ArrayList<>();

	private BackLogProcessor() {
		setDaemon( true );
	}

	static class SingletonHolder {
		static BackLogProcessor instance = new BackLogProcessor();
	}

	public static BackLogProcessor getInstance() {
		return SingletonHolder.instance;
	}

	public Collection<Task> getPendingTasks() {
		return pendingTasks;
	}
	
	public List<TaskExecutor> getRunningExecutors() {
		return runningExecutors;
	}
	
	@Override
	public void run() {

		try {
			for (;;) {

				if (shutdownRequested) {
					break;
				}
				
				runningExecutors = runningExecutors.stream().filter( executor-> !executor.isFinished() ).collect( Collectors.toList() );

				LocalDateTime now = LocalDateTime.now();
				
				Optional<Task> selectedTask = pendingTasks.stream()
					.filter( task -> !blackListedTaskClass.contains( task.getClass() ) )
					.filter( task -> !(task instanceof Enableable) || ((Enableable) task).isEnabled() )
					.filter( task -> task.getMinDate() == null || task.getMinDate().isBefore( now ) )
					.findFirst();
				
				if (!selectedTask.isPresent()) {
					Thread.sleep( 2000 );
					continue;
				}
				
				Task task = selectedTask.get();
				
				if (!(task instanceof DaemonTask)) {
					pendingTasks.remove( task );
				} else {
					// daemon tasks stay in the pending tasks list forever
					DaemonTask daemon = (DaemonTask) task;
					daemon.setMinDate( LocalDateTime.now().plusMinutes( daemon.getMinutesFrequency() ) );
				}

				Class<? extends TaskExecutor> backLogTaskClass = ConfigurationManager.getInstance().getActivePlugin( task.getClass() );
				if (backLogTaskClass != null) {
					TaskExecutor<Task> executor = ConfigurationManager.getInstance().newExecutorInstance( backLogTaskClass, task );
					runningExecutors.add( executor );
					futures.add( pool.submit( executor ) );
				} else {
					ErrorManager.getInstance().reportWarning( String.format( "No Executor capable of executing '%s' was found", task ));				
					blackListedTaskClass.add( task.getClass() );
				}				
				
			}
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		} finally {
			pool.shutdownNow();
		}
	}

	public void schedule( Task task ) {
		schedule( task, true );
	}

	public Task schedule( Task task, boolean reportQueued ) {
		if (task instanceof Enableable && !((Enableable) task).isEnabled()) {
			return null;
		}

		pendingTasks.remove( task );

		if (task instanceof LogQueuing) {
			ErrorManager.getInstance().reportDebug(task, String.format("%s was queued", task.toString()));
		}
		
		pendingTasks.add( task );
		if (reportQueued) {
			EventManager.getInstance().reportInfo( String.format("%s has been queued", task.toString()));
		}
		
		return task;
	}

	public void shutdown() {
		shutdownRequested = true;
	}

	public void runNow( Task item, boolean reportQueued ) {
		unschedule( item );
		item.setMinDate( null );
		schedule( item, reportQueued );
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
		boolean isRunning = runningExecutors.stream().filter( executor-> executor.isRunning() && match( executor.getTask(), taskClass, null )).findAny().isPresent();
		if (isRunning) {
			return true;
		}
		return pendingTasks.stream().filter( task -> match( task, taskClass, null)).findAny().isPresent();
	}
	
	public void unschedule( Class<? extends Task> taskClass, String expressionToVerify ) {
		runningExecutors.stream().filter( executor-> executor.isRunning() && match( executor.getTask(), taskClass, expressionToVerify )).forEach( executor -> executor.cancel() );
		pendingTasks.stream().filter( task -> match( task, taskClass, expressionToVerify) ).forEach( task -> unschedule( task ) );
	}


	private void unschedule(Task task) {
		pendingTasks.remove( task );
		runningExecutors.stream().filter( executor-> executor.isRunning() && executor.getTask().equals( task )).forEach( executor -> executor.cancel() );
	}
	
	public void cancel(Task task) {
		unschedule(task);
		if (task instanceof CancellableTask) { ((CancellableTask) task).cancel(); }
	}	

	public void runSync(Task task, boolean reportQueued) throws Exception {
		Class<? extends TaskExecutor> backLogTaskClass = ConfigurationManager.getInstance().getActivePlugin( task.getClass() );
		if (backLogTaskClass != null) {
			TaskExecutor<Task> executor = ConfigurationManager.getInstance().newExecutorInstance( backLogTaskClass, task );
			executor.execute();
		}
	}

}
