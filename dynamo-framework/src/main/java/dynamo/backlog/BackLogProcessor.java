package dynamo.backlog;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.el.ExpressionFactory;

import dynamo.core.Enableable;
import dynamo.core.EventManager;
import dynamo.core.el.DynamoELContext;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.CancellableTask;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;

public class BackLogProcessor extends Thread {

	private boolean shutdownRequested = false;
	protected Map<String, AbstractDynamoQueue> queues = new ConcurrentHashMap<String, AbstractDynamoQueue>();
	private BlockingQueue<Task> items = new LinkedBlockingQueue<Task>();
	private Set<Class> blackListedTasks = new HashSet<>();

	private BackLogProcessor() {
		setDaemon( true );
	}

	static class SingletonHolder {
		static BackLogProcessor instance = new BackLogProcessor();
	}

	public static BackLogProcessor getInstance() {
		return SingletonHolder.instance;
	}

	public Map<String, AbstractDynamoQueue> getQueues() {
		return queues;
	}

	public Collection<Task> getItems() {
		return items;
	}
	
	protected AbstractDynamoQueue getQueueForTask( Task task ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException {
		AbstractDynamoQueue queue = (AbstractDynamoQueue) queues.get( task.getQueueClass().getName() );

		if (queue == null) {
			queue = (AbstractDynamoQueue) ConfigurationManager.configureQueue( task.getQueueClass() );
			queues.put( task.getQueueClass().getName(), queue );
		}	
		
		return queue;
	}
	
	@Override
	public void run() {

		try {
			for (;;) {

				if (shutdownRequested) {
					break;
				}

				Date now = new Date();

				Task currentItem = null;
				for (Task item : items) {
					
					if (blackListedTasks.contains( item.getClass() )) {
						continue;
					}
					
					if (item instanceof Enableable && !((Enableable) item).isEnabled()) {
						continue;
					}
					if (item.getMinDate() == null || item.getMinDate().before( now )) {
						currentItem = item;
						break;
					}
				}
								
				if (currentItem == null) {
					Thread.sleep( 1000 );
					continue;
				}
				
				items.remove( currentItem );

				AbstractDynamoQueue queue = getQueueForTask( currentItem );
				if (!queue.executeTask( currentItem )) {
					// blacklist this task class : no executor can execute it
					blackListedTasks.add( currentItem.getClass() );
				}
				
			}
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		} finally {
		
			Collection<AbstractDynamoQueue> values = queues.values();
			for (AbstractDynamoQueue queue : values) {
				queue.shutdownNow();
			}
		}
	}

	public void schedule( Task item ) {
		schedule( item, true );
	}

	public void schedule( Task item, boolean reportQueued ) {
		if (item instanceof Enableable && !((Enableable) item).isEnabled()) {
			return;
		}
		if (!items.contains( item )) {
			for (AbstractDynamoQueue queue : getQueues().values()) {
				if (queue.getTaskBackLog().contains(item)) {
					// already scheduled in queue
					return;
				}
			}
			items.add( item );
			if (reportQueued) {
				EventManager.getInstance().reportInfo( String.format("%s has been queued", item.toString()));
			}
		}
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

		if (taskClass == null || task.getClass().equals( taskClass )) {
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

	public void unschedule( Class<? extends Task> taskClass, String expressionToVerify ) {
		for (AbstractDynamoQueue queue : getQueues().values()) {
			for (Task task : queue.getTaskBackLog()) {
				if (match( task, taskClass, expressionToVerify)) {
					queue.cancel(task);
				}
			}
		}
		for (Task task : items) {
			if (match( task, taskClass, expressionToVerify)) {
				unschedule(task);
			}
		}
	}

	private void unschedule(Task task) {
		items.remove( task );
		for (AbstractDynamoQueue queue : getQueues().values()) {
			queue.cancel( task );
		}
	}
	
	public void cancel(Task task) {
		unschedule(task);
		if (task instanceof CancellableTask) { ((CancellableTask) task).cancel(); }
	}	

	public void runSync(Task task, boolean reportQueued) throws Exception {
		Class<? extends TaskExecutor<?>> backLogTaskClass = ConfigurationManager.getInstance().getActivePlugin( task.getClass() );
		if (backLogTaskClass != null) {
			TaskExecutor<Task> executor = ConfigurationManager.getInstance().newExecutorInstance( backLogTaskClass, task );
			executor.execute();
		}
	}

}
