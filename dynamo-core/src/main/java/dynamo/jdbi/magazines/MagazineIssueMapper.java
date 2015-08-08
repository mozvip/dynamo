package dynamo.jdbi.magazines;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.core.Language;
import dynamo.core.model.MapperUtils;
import dynamo.model.DownloadableStatus;
import dynamo.model.magazines.MagazineIssue;

public class MagazineIssueMapper implements ResultSetMapper<MagazineIssue> {

	@Override
	public MagazineIssue map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new MagazineIssue(
				r.getLong("ID"),
				MapperUtils.getPath(r, "PATH"),
				MapperUtils.getEnum(r, "STATUS", DownloadableStatus.class),
				r.getString("AKA"),
				r.getString("MAGAZINE_SEARCHNAME"),
				MapperUtils.getEnum(r, "LANGUAGE", Language.class),
				r.getString("RAWNAME"),
				r.getDate("ISSUEDATE"),
				r.getInt("YEAR"),
				r.getInt("ISSUE"),
				r.getBoolean("SPECIAL"),
				r.getString("COVER_IMAGE"),
				r.getDate("CREATION_DATE")
		);
	}

}
