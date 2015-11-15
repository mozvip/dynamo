package dynamo.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.CancellableTask;
import dynamo.core.model.Task;

@ManagedBean(name="backlog")
public class Backlog extends DynamoManagedBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Collection<AbstractDynamoQueue> getQueues() {
		return new ArrayList<AbstractDynamoQueue>( BackLogProcessor.getInstance().getQueues().values() );
	}
	
	public Collection<Task> getItems() {
		return BackLogProcessor.getInstance().getItems();
	}
	
	public void runNow( Task item ) {
		runNow( item, false );
	}
	
	public void cancel( Task item ) {
		BackLogProcessor.getInstance().cancel( item );
	}

	public String getRowClasses( AbstractDynamoQueue queue ) {
		List<String> classes = queue.getBackLog().map( executor -> executor.isRunning() ? "success" : "").collect(Collectors.toList());
		return StringUtils.join(classes, ",");
	}
	
	public boolean isCancellable(Task t) {
		return t instanceof CancellableTask;
	}

}
