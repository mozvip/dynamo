package dynamo.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.ErrorManager;

public abstract class AbstractDynamoQueue {

	private int queueSize;
	private List<TaskExecutor<Task>> submittedExecutors = new ArrayList<>();
	private ThreadPoolExecutor pool;

	public abstract String getQueueName();

	public AbstractDynamoQueue( int defaultQueueSize ) {
		this.queueSize = defaultQueueSize;
		pool = (ThreadPoolExecutor) Executors.newFixedThreadPool( getQueueSize(), new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName( String.format("%s-%d", getQueueName(), System.currentTimeMillis()));
				return thread;
			}
		} );
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
		if (pool != null && queueSize >= pool.getCorePoolSize() ) {
			pool.setMaximumPoolSize( queueSize );
		}
	}
	
	public void cancel(Task task) {
		Optional<TaskExecutor<Task>> executorToCancel =
				submittedExecutors.stream()
				.filter( executor -> executor.getTask().equals( task ) )
				.findFirst();
		if (executorToCancel.isPresent()) {
			TaskExecutor<Task> executor = executorToCancel.get();
			if (!executor.isFinished()) {
				executor.cancel();
			}
		}
	}
	
	public List<TaskExecutor<Task>> getBackLog() {
		return submittedExecutors;
	}

	public List<Task> getTaskBackLog() {
		return submittedExecutors.stream()
				.map( executor -> executor.getTask() )
				.collect( Collectors.toList() );
	}
	
	public boolean isExecuting( Task task ) {
		return submittedExecutors.stream()
				.anyMatch( executor -> executor.getTask().equals( task ));
	}

	public BlockingQueue<Runnable> getQueue() {
		return pool.getQueue();
	}
	
	public List<FutureTask<?>> getFutures() {
		return futures;
	}
	
	public List<FutureTask<?>> futures = new ArrayList<FutureTask<?>>();

	public boolean executeTask( Task task ) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		synchronized (this) {
			Class<? extends TaskExecutor<?>> backLogTaskClass = ConfigurationManager.getInstance().getActivePlugin( task.getClass() );
			if (backLogTaskClass != null) {
				TaskExecutor<Task> executor = ConfigurationManager.getInstance().newExecutorInstance( backLogTaskClass, task );
				if (!isExecuting(task)) {
					submittedExecutors.add ( executor );
					futures.add( (FutureTask<?>) pool.submit( executor ) );
				}
				return true;
			} else {
				ErrorManager.getInstance().reportWarning( String.format( "No Executor capable of executing '%s' was found", task ));				
			}
			return false;
		}
	}

	public void shutdownNow() {
		pool.shutdownNow();
	}

	public void refresh() {
		synchronized (this) {
			submittedExecutors.removeIf( executor -> executor == null || executor.isFinished() );		
		}
	}

}
