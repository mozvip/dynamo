package com.github.dynamo.core.logging;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class StackTraceElementMapper implements ResultSetMapper<StackTraceElement> {

	@Override
	public StackTraceElement map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return new StackTraceElement(r.getString("DECLARINGCLASS"), r.getString("METHODNAME"), r.getString("FILENAME"), r.getInt("LINENUMBER"));
	}

}
