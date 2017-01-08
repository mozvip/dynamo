package com.github.dynamo.core.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dynamo.jdbi.core.DAO;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class DAOManager {

	private static final String DATABASE_URL_PATTERN = "jdbc:h2:./%s;MVCC=TRUE;LOCK_TIMEOUT=10000";
	private final static Logger LOGGER = LoggerFactory.getLogger(DAOManager.class);

	static class SingletonHolder {
		private SingletonHolder() {
		}

		static DAOManager instance = new DAOManager();
	}

	protected String getJdbcUrl(String databaseId) {
		return String.format(DATABASE_URL_PATTERN, databaseId);
	}

	LoadingCache<String, JdbcConnectionPool> connectionPools = CacheBuilder.newBuilder().build(new CacheLoader<String, JdbcConnectionPool>() {
		public JdbcConnectionPool load(String databaseId) {
			return JdbcConnectionPool.create(getJdbcUrl(databaseId), "dynamo", "dynamo");
		}

	});

	LoadingCache<String, DBI> dbiInstances = CacheBuilder.newBuilder().build(new CacheLoader<String, DBI>() {
		public DBI load(String databaseId) throws ExecutionException {
			return new DBI(connectionPools.get(databaseId));
		}
	});
	
	LoadingCache<Class<?>, Object> daoInstances = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, Object>() {
		public Object load(Class<?> daoInterface) throws ExecutionException {
			DAO daoAnnotation = daoInterface.getAnnotation(DAO.class);
			if (daoAnnotation == null && daoInterface.getInterfaces() != null) {
				for (Class<?> interf : daoInterface.getInterfaces()) {
					daoAnnotation = interf.getAnnotation(DAO.class);
					if (daoAnnotation != null) {
						break;
					}
				}
			}
			String databaseId = daoAnnotation.databaseId();
			try {
				DBI dbi = dbiInstances.get(databaseId);
				return dbi.onDemand(daoInterface);
			} catch (ExecutionException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
			return null;
		}
	});

	public Connection getSingleConnection(String databaseId) {
		try {
			Class.forName("org.h2.Driver");
			return (DriverManager.getConnection( getJdbcUrl(databaseId), "dynamo", "dynamo"));
		} catch (ClassNotFoundException | SQLException e) {
			ErrorManager.getInstance().reportThrowable( e );
			return null;
		}
	}

	public static DAOManager getInstance() {
		return SingletonHolder.instance;
	}

	public <E> E getDAO(Class<E> daoKlass) {
		try {
			return (E) daoInstances.get(daoKlass);
		} catch (ExecutionException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		return null;
	}

	@Override
	protected void finalize() throws Throwable {
		for (JdbcConnectionPool connectionPool : connectionPools.asMap().values()) {
			connectionPool.dispose();
		}
		super.finalize();
	}

}
