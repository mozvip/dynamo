package dynamo.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

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
	
	public List<TaskExecutor<Task>> getSubmittedExecutors() {
		return submittedExecutors;
	}
	
	public void cancel(Task task) {
		synchronized (this) {
			for (Iterator<TaskExecutor<Task>> iterator = submittedExecutors.iterator(); iterator.hasNext();) {
				TaskExecutor<Task> executor = iterator.next();
				if (executor.getTask().equals( task ) && (!executor.isDone())) {
					executor.cancel();
					iterator.remove();
					return;
				}
			}
		}
	}
	
	public List<TaskExecutor<Task>> getBackLog() {
		List<TaskExecutor<Task>> executors = new ArrayList<>();
		synchronized (this) {
			// clean done tasks
			for (Iterator<TaskExecutor<Task>> iterator = submittedExecutors.iterator(); iterator.hasNext();) {
				TaskExecutor<Task> executor = iterator.next();
				if (executor != null && !executor.isDone()) {
					executors.add( executor );
				} else {
					// clean done tasks
					iterator.remove();
				}
			}
		}
		return executors;
	}

	public List<Task> getTaskBackLog() {
		List<Task> itemsBacklog = new ArrayList<Task>();
		List<TaskExecutor<Task>> executors = getBackLog();
		for (TaskExecutor<Task> taskExecutor : executors) {
			itemsBacklog.add( taskExecutor.getTask() );
		}
		return itemsBacklog;
	}


	public BlockingQueue<Runnable> getQueue() {
		return pool.getQueue();
	}
	
	public List<FutureTask<?>> getFutures() {
		return futures;
	}
	
	public List<FutureTask<?>> futures = new ArrayList<FutureTask<?>>();

	public synchronized boolean executeTask( Task task ) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		synchronized (this) {
			if (getTaskBackLog().contains( task )) {
				return true;
			}
			Class<? extends TaskExecutor<?>> backLogTaskClass = ConfigurationManager.getInstance().getActivePlugin( task.getClass() );
			if (backLogTaskClass != null) {
				if (!getTaskBackLog().contains( task )) {
					TaskExecutor<Task> executor = ConfigurationManager.getInstance().newExecutorInstance( backLogTaskClass, task );
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

}
