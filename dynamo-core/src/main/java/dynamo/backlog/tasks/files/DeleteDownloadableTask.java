package dynamo.backlog.tasks.files;

import java.lang.reflect.InvocationTargetException;

import dynamo.backlog.tasks.core.ImmediateTask;
import dynamo.core.LogQueuing;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.model.DownloadableTask;
import dynamo.model.Downloadable;

public class DeleteDownloadableTask extends DownloadableTask implements ImmediateTask, LogQueuing {
	
	public DeleteDownloadableTask( long id ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		this( DownloadableFactory.getInstance().createInstance(id) );
	}

	public DeleteDownloadableTask( Downloadable downloadable ) {
		super(downloadable);
	}

	@Override
	public String toString() {
		return String.format("Delete %s", downloadable.toString());
	}

}
