package dynamo.core.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dynamo.core.model.DownloadableDAO;
import dynamo.model.DownloadInfo;
import dynamo.model.Downloadable;

public class DownloadableFactory {

	private DownloadableDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableDAO.class );
	
	private Map<Class<? extends Downloadable>, Method> factoryMethodsMap = new HashMap<>();
	
	static class SingletonHolder {
		static DownloadableFactory instance = new DownloadableFactory();
	}
	
	public static DownloadableFactory getInstance() {
		return SingletonHolder.instance;
	}		

	private DownloadableFactory() {
	}
	
	public Downloadable createInstance( long downloadableId ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		return createInstance( getDownloadInfo(downloadableId) );
	}
	
	public synchronized Downloadable createInstance( DownloadInfo downloadInfo ) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		Class<Downloadable> klass = downloadInfo.getDownloadableClass();
		Method factoryMethod = null;
		if( factoryMethodsMap.containsKey( klass )) {
			factoryMethod = factoryMethodsMap.get( klass );
		} else {
			factoryMethod = getMethod(klass);
			factoryMethodsMap.put( klass, factoryMethod );
		}
		Object daoInstance = DAOManager.getInstance().getDAO( factoryMethod.getDeclaringClass() );
		return (Downloadable) factoryMethod.invoke( daoInstance, downloadInfo.getId() );
	}

	Method getMethod( Class<? extends Downloadable> classToCreate ) {
		
		// find corresponding DAO method that takes a long as a parameter and return the expected object type
		DynamoObjectFactory factory = new DynamoObjectFactory("dynamo", ".*DAO");
		Set<Class> daoInterfaces = factory.getMatchingClasses( false, true );
		for (Class daoInterface : daoInterfaces) {
			Method[] methods = daoInterface.getMethods();
			for (Method method : methods) {
				if (method.getReturnType().equals( classToCreate )) {
					if (method.getParameterTypes().length == 1) {
						Class parameterType = method.getParameterTypes()[0];
						if (parameterType.equals(Long.class) || parameterType.equals(long.class)) {
							return method;
						}
					}
				}
			}
		}
		
		ErrorManager.getInstance().reportInfo( String.format("No factory method to create instance of %s was found", classToCreate.getName() ));

		return null;
	}

	public DownloadInfo getDownloadInfo(long id) {
		return downloadableDAO.find(id);
	}

}
