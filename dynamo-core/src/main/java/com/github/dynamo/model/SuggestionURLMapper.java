package com.github.dynamo.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class SuggestionURLMapper implements ResultSetMapper<SuggestionURL> {

	@Override
	public SuggestionURL map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new SuggestionURL( r.getLong("DOWNLOADABLE_ID"), r.getString("SUGGESTION_URL"));
	}

}
