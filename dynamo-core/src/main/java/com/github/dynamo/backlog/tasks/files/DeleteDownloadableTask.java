package com.github.dynamo.backlog.tasks.files;

import java.lang.reflect.InvocationTargetException;

import com.github.dynamo.backlog.tasks.core.ImmediateTask;
import com.github.dynamo.core.LogQueuing;
import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.core.model.DownloadableTask;
import com.github.dynamo.model.Downloadable;

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
