package dynamo.backlog.tasks.files;

import java.lang.reflect.InvocationTargetException;

import dynamo.core.DynamoTask;
import dynamo.core.LogQueuing;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.model.DownloadableTask;
import dynamo.model.Downloadable;

@DynamoTask(queueClass=DeleteDownloadableQueue.class)
public class DeleteDownloadableTask extends DownloadableTask implements LogQueuing {
	
	public DeleteDownloadableTask( long id ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		this( DownloadableFactory.getInstance().createInstance(id) );
	}

	public DeleteDownloadableTask( Downloadable downloadable ) {
		super(downloadable);
	}

	@Override
	public String toString() {
		return String.format("Delete <a href='%s'>%s</a>", downloadable.getRelativeLink(), downloadable.toString());
	}

}
