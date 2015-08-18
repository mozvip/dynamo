package dynamo.core.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.h2.jdbcx.JdbcConnectionPool;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.jdbi.core.DAO;

public class DAOManager {
	
	private final static Logger LOGGER = LoggerFactory.getLogger( DAOManager.class );
	
	protected static Map<String, JdbcConnectionPool> connectionPools = new HashMap<>();
	protected static Map<String, DBI> dbiInstances = new HashMap<>();

	protected static Map<Class<?>, Object> daoInstances = new HashMap<>();

	static class SingletonHolder {
		private SingletonHolder() {}
		static DAOManager instance = new DAOManager();
	}
	
	public JdbcConnectionPool getDatasource( String databaseId ) {
		return connectionPools.get(databaseId);
	}
	
	public DBI getDBIInstance(String database) {
		return dbiInstances.get( database );
	}

	private DAOManager() {
		
		Collection<Class<?>> daoInterfaces = new DynamoObjectFactory(ConfigurationManager.DYNAMO_PACKAGE_PREFIX, ".*DAO").getMatchingClasses(false, true);

		for (Class<?> daoInterface : daoInterfaces) {
			
			if (!daoInterface.isInterface()) {
				continue;
			}
			
			DAO daoAnnotation = daoInterface.getAnnotation( DAO.class );
			if (daoAnnotation == null) {
				LOGGER.warn( String.format("%s has several characteristics of a DAO interface, but it isn't annotated with @DAO", daoInterface.getName()));
			} else {
				String databaseId = daoAnnotation.databaseId();
				JdbcConnectionPool connectionPool = null;
				
				DBI dbi = null;
				if ( connectionPools.containsKey(databaseId )) {
					connectionPool = connectionPools.get(databaseId);
					dbi = dbiInstances.get( databaseId );
				} else {
					connectionPool = JdbcConnectionPool.create( String.format("jdbc:h2:./%s;MVCC=TRUE;LOCK_TIMEOUT=10000", databaseId), "dynamo", "dynamo" );
					connectionPools.put( databaseId, connectionPool );
					dbi = new DBI( connectionPool );
					dbiInstances.put( databaseId, dbi );
				}
				daoInstances.put(daoInterface, dbi.onDemand(daoInterface));	
			}
		}
	}

	public static DAOManager getInstance() {
		return SingletonHolder.instance;
	}	

	public <E> E getDAO(Class<E> daoKlass) {
		return (E) daoInstances.get( daoKlass );
	}

	@Override
	protected void finalize() throws Throwable {
		for (JdbcConnectionPool connectionPool : connectionPools.values()) {
			connectionPool.dispose();
		}
		super.finalize();
	}
	
}
