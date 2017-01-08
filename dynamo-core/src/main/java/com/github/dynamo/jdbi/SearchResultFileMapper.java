package com.github.dynamo.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultFile;

public class SearchResultFileMapper implements ResultSetMapper<SearchResultFile> {

	@Override
	public SearchResultFile map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		SearchResult result = new SearchResultMapper().map(index, r, ctx);
		return new SearchResultFile( r.getLong("ID"), r.getString("NAME"), r.getLong("SIZE"), result);
	}


}
