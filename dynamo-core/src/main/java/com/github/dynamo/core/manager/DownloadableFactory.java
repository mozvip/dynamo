package com.github.dynamo.core.manager;

import java.lang.reflect.InvocationTargetException;

import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.DownloadableDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.DownloadInfo;
import com.github.dynamo.model.Downloadable;

public class DownloadableFactory {

	static class SingletonHolder {
		static DownloadableFactory instance = new DownloadableFactory();
	}
	
	public static DownloadableFactory getInstance() {
		return SingletonHolder.instance;
	}		

	private DownloadableFactory() {
	}
	
	public synchronized Downloadable createInstance( long downloadableId ) {
		DownloadInfo downloadInfo = DownloadableManager.getInstance().find( downloadableId );
		if (downloadInfo != null) {
			try {
				return createInstance(downloadableId, downloadInfo.getDownloadableClass());
			} catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
		return null;
	}
	
	public synchronized Downloadable createInstance( long downloadableId, Class<? extends Downloadable> klass ) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, IllegalArgumentException, NoSuchMethodException, SecurityException {
		DownloadableDAO daoInstance = DownloadableManager.getInstance().getDAOInstance( klass );
		return (Downloadable) daoInstance.getClass().getMethod("find", long.class).invoke( daoInstance, downloadableId );
	}

}
