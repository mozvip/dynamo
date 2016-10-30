package dynamo.music.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.core.model.MapperUtils;
import dynamo.model.music.MusicFile;

public class MusicFileMapper implements ResultSetMapper<MusicFile> {

	@Override
	public MusicFile map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return new MusicFile(
				r.getLong("FILE_ID"),
				r.getLong("DOWNLOADABLE_ID"),
				MapperUtils.getPath(r, "FILE_PATH"),
				r.getInt("FILE_INDEX"),
				r.getLong("SIZE"),
				r.getString("FILE_IDENTIFIER"),
				r.getString("SONGARTIST"),
				r.getString("SONGTITLE"),
				r.getInt("YEAR"),
				r.getBoolean("TAGSMODIFIED"));
	}

}
