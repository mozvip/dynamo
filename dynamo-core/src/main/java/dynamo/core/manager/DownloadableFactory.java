package dynamo.core.manager;

import java.lang.reflect.InvocationTargetException;

import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadInfo;
import dynamo.model.Downloadable;

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
		try {
			return createInstance(downloadableId, downloadInfo.getDownloadableClass());
		} catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		return null;
	}
	
	public synchronized Downloadable createInstance( long downloadableId, Class<? extends Downloadable> klass ) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		return DownloadableManager.getInstance().getDAOInstance( klass ).find( downloadableId );
	}

}
