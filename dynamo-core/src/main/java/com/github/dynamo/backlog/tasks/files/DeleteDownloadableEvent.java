package com.github.dynamo.backlog.tasks.files;

import java.lang.reflect.InvocationTargetException;

import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.model.Downloadable;

public class DeleteDownloadableEvent {
	
	private Downloadable downloadable; 
	
	public DeleteDownloadableEvent( long id ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		this( DownloadableFactory.getInstance().createInstance(id) );
	}

	public DeleteDownloadableEvent( Downloadable downloadable ) {
		this.downloadable = downloadable;
	}
	
	public Downloadable getDownloadable() {
		return downloadable;
	}

	@Override
	public String toString() {
		return String.format("Delete %s", downloadable.toString());
	}

}
