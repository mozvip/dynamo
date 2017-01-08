package com.github.dynamo.core.logging;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;


public class LogItemMapper implements ResultSetMapper<LogItem> {

	@Override
	public LogItem map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return new LogItem( r.getLong("ID"), r.getTimestamp("DATE"), r.getString("MESSAGE"), LogItemSeverity.valueOf( r.getString("SEVERITY") ), r.getString("TASKNAME"));
	}

}
