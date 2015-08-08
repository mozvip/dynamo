package dynamo.core.model;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.DAO;
import dynamo.model.DownloadableStatus;
import dynamo.model.HistoryItem;

@DAO(databaseId="dynamo")
public interface HistoryDAO {

	@SqlUpdate("INSERT INTO HISTORYITEM (COMMENT, DATE, STATUS, DOWNLOADABLE_ID) VALUES (:comment, CURRENT_TIMESTAMP(), :status, :downloadableId)")
	void insert( @Bind("comment") String comment, @BindEnum("status") DownloadableStatus status, @Bind("downloadableId") long downloadableId );

	@SqlQuery("SELECT * FROM HISTORYITEM ORDER BY DATE DESC")
	@Mapper(HistoryMapper.class)
	List<HistoryItem> findAll();

}
