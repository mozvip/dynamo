package com.github.dynamo.core.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.github.dynamo.core.model.DownloadableDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.DownloadInfo;
import com.github.dynamo.model.Downloadable;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class DownloadableFactory {

	static class SingletonHolder {
		static DownloadableFactory instance = new DownloadableFactory();
	}
	
	public static DownloadableFactory getInstance() {
		return SingletonHolder.instance;
	}
	
	Map<Class<? extends Downloadable>, DownloadableDAO> downloadableDaos;

	private DownloadableFactory() {
		
		downloadableDaos = new HashMap<>();
		Set<Class<? extends DownloadableDAO>> daos = DynamoObjectFactory.getReflections().getSubTypesOf(DownloadableDAO.class);
		for (Class<? extends DownloadableDAO> dao : daos) {
			try {
				Class<? extends Downloadable> downloadableType = (Class<? extends Downloadable>) dao.getDeclaredMethod("find", long.class).getReturnType();
				downloadableDaos.put(downloadableType, DAOManager.getInstance().getDAO( dao));
			} catch (NoSuchMethodException | SecurityException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
		
	}

	public DownloadableDAO getDAOInstance(Class<? extends Downloadable> klass) {
		return downloadableDaos.get(klass);
	}

	private LoadingCache<Class<? extends Downloadable>, Method> findMethods = CacheBuilder.newBuilder()
		       .build(
		           new CacheLoader<Class<? extends Downloadable>, Method>() {
		             public Method load(Class<? extends Downloadable> klass) throws NoSuchMethodException, SecurityException, ExecutionException {
		            	 DownloadableDAO daoInstance = getDAOInstance(klass);
		            	 return daoInstance.getClass().getMethod("find", long.class);
		             }
		           });	
	
	public Downloadable createInstance( long downloadableId ) {
		DownloadInfo downloadInfo = DownloadableManager.getInstance().find( downloadableId );
		if (downloadInfo != null) {
			try {
				return createInstance(downloadableId, downloadInfo.getDownloadableClass());
			} catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | ExecutionException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
		return null;
	}
	
	public Downloadable createInstance( long downloadableId, Class<? extends Downloadable> klass ) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, IllegalArgumentException, NoSuchMethodException, SecurityException, ExecutionException {
		return (Downloadable) findMethods.get(klass).invoke( getDAOInstance(klass), downloadableId );
	}

}
