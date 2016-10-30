package dynamo.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.core.model.DownloadableFile;
import dynamo.core.model.MapperUtils;

public class DownloadableFileMapper implements ResultSetMapper<DownloadableFile> {

	@Override
	public DownloadableFile map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new DownloadableFile(
				r.getLong("FILE_ID"),
				r.getLong("DOWNLOADABLE_ID"),
				MapperUtils.getPath(r, "FILE_PATH"),
				r.getInt("FILE_INDEX"),
				r.getLong("SIZE"),
				r.getString("FILE_IDENTIFIER")
				);
	}

}
