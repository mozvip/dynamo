package dynamo.core.logging;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.DAO;

@DAO(databaseId="core")
public interface LogDAO {

	@SqlUpdate("INSERT INTO LOGITEM(DATE, MESSAGE, SEVERITY, TASKNAME) VALUES( CURRENT_TIMESTAMP(), :message, :severity, :taskName )")
	@GetGeneratedKeys
	public long create( @Bind("message") String message, @BindEnum("severity") LogItemSeverity severity, @Bind("taskName") String taskName );
	
	@SqlBatch("INSERT INTO LOGSTACKTRACEELEMENT(DECLARINGCLASS, FILENAME, LINENUMBER, METHODNAME, LOGITEM_ID) VALUES( :className, :fileName, :lineNumber, :methodName, :logItemId)")
	public void insertStackTrace( @BindBean StackTraceElement[] stackTraceElements, @Bind("logItemId") long logItemId );

	@SqlUpdate("DELETE FROM LOGITEM WHERE ID = :id")
	public void delete(@Bind("id") long id);

	@SqlUpdate("DELETE FROM LOGITEM")
	public void deleteAll();

	// FIXME
	@SqlQuery("SELECT * FROM LOGITEM WHERE INSTR(:concatenatedSeverities, SEVERITY) > 0 ORDER BY DATE DESC")
	@Mapper(LogItemMapper.class)
	public List<LogItem> findLogItems(@Bind("concatenatedSeverities") String concatenatedSeverities);

	@SqlQuery("SELECT * FROM LOGITEM ORDER BY DATE DESC")
	@Mapper(LogItemMapper.class)
	public List<LogItem> findLogItems();

	@SqlQuery("SELECT * FROM LOGSTACKTRACEELEMENT WHERE LOGITEM_ID = :id")
	@Mapper(StackTraceElementMapper.class)
	public List<StackTraceElement> getStackTrace(@Bind("id") long id);

}
