package com.github.dynamo.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.github.dynamo.core.model.MapperUtils;
import com.github.dynamo.tvshows.model.UnrecognizedFile;

public class UnrecognizedFileMapper implements ResultSetMapper<UnrecognizedFile> {
	
	@Override
	public UnrecognizedFile map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return new UnrecognizedFile(r.getLong("ID"), MapperUtils.getPath(r, "PATH"), r.getString("SERIES_ID"));
	}

}
