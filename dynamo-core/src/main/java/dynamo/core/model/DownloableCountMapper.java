package dynamo.core.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.model.DownloadableStatus;

public class DownloableCountMapper implements ResultSetMapper<DownloableCount> {

	@Override
	public DownloableCount map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		try {
			return new DownloableCount(Class.forName(r.getString("DTYPE")).getSimpleName(),
					MapperUtils.getEnum(r, "STATUS", DownloadableStatus.class), r.getInt("C"));
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
	}

}
