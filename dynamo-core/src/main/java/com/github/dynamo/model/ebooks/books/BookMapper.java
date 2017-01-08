package com.github.dynamo.model.ebooks.books;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.model.MapperUtils;
import com.github.dynamo.model.DownloadableStatus;

public class BookMapper implements ResultSetMapper<Book> {

	@Override
	public Book map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new Book(
				r.getLong("ID"),
				MapperUtils.getEnum(r, "STATUS", DownloadableStatus.class),
				r.getString("AKA"),
				r.getString("NAME"),
				r.getString("AUTHOR"),
				MapperUtils.getEnum(r, "LANGUAGE", Language.class),
				r.getDate("CREATION_DATE")
				);
	}

}
