package hclient.cache;

import java.io.Serializable;
import java.sql.Timestamp;

import org.skife.jdbi.v2.DBI;

public class ClientCache {
	
	private DBI dbi = new DBI("jdbc:h2:./httpclient-cache"); 
	private CacheDAO dao;

	static class SingletonHolder {
		static ClientCache instance = new ClientCache();
	}
	
	public static ClientCache getInstance() {
		return SingletonHolder.instance;
	}
	
	private ClientCache() {
		dao = dbi.open( CacheDAO.class);
		dao.createTable();
		dao.purge();
	}

	public Serializable getFromCache(String key) {
		return dao.getCachedValue(key);
	}

	public void putInCache( String key, Serializable content, long cacheRefreshPeriod ) {
		dao.putValueInCache(key, content, new Timestamp( System.currentTimeMillis() + cacheRefreshPeriod) );
	}
	
	public void removeCache( String key ) {
		dao.removeCache( key );
	}

}
