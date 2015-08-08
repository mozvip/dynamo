package hclient.cache;

import java.io.Serializable;
import java.sql.Timestamp;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

public interface CacheDAO {
	
	@SqlUpdate("CREATE TABLE IF NOT EXISTS CACHE (KEY VARCHAR(4096) PRIMARY KEY, VALUE OTHER NOT NULL, EXPIRATION_TIME TIMESTAMP NOT NULL)")
	public void createTable();

	@SqlQuery("SELECT VALUE FROM CACHE WHERE KEY=:key AND EXPIRATION_TIME > CURRENT_TIMESTAMP()")
	@Mapper(SerializableResultSetMapper.class)
	public Serializable getCachedValue(@Bind("key") String key);
	
	@SqlUpdate("MERGE INTO CACHE VALUES (:key, :value, :expirationTime)")
	public void putValueInCache(@Bind("key") String key, @Bind("value") Serializable value, @Bind("expirationTime") Timestamp expirationTime);

	@SqlUpdate("DELETE FROM CACHE WHERE EXPIRATION_TIME < CURRENT_TIMESTAMP()")
	public void purge();

	@SqlUpdate("DELETE FROM CACHE WHERE KEY = :key")
	public void removeCache(@Bind("key") String key);

}
