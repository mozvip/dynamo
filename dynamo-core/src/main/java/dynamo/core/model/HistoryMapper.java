package dynamo.core.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.model.DownloadableStatus;
import dynamo.model.HistoryItem;

public class HistoryMapper implements ResultSetMapper<HistoryItem> {

	@Override
	public HistoryItem map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return new HistoryItem( r.getLong("ID"), r.getString("COMMENT"), r.getTimestamp("DATE"), MapperUtils.getEnum(r, "STATUS", DownloadableStatus.class), r.getLong("DOWNLOADABLE_ID"));
	}

}
