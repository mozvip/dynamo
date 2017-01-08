package com.github.dynamo.magazines.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.model.MapperUtils;
import com.github.dynamo.magazines.model.MagazineIssue;
import com.github.dynamo.model.DownloadableStatus;

public class MagazineIssueMapper implements ResultSetMapper<MagazineIssue> {

	@Override
	public MagazineIssue map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new MagazineIssue(
				r.getLong("ID"),
				MapperUtils.getEnum(r, "STATUS", DownloadableStatus.class),
				r.getString("AKA"),
				r.getString("MAGAZINE_SEARCHNAME"),
				MapperUtils.getEnum(r, "LANGUAGE", Language.class),
				r.getString("NAME"),
				r.getDate("ISSUEDATE"),
				r.getInt("YEAR"),
				r.getInt("ISSUE"),
				r.getBoolean("SPECIAL"),
				r.getDate("CREATION_DATE")
		);
	}

}
