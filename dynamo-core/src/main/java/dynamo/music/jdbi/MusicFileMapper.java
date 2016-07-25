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
		return new MusicFile(MapperUtils.getPath(r, "PATH"),
				r.getLong("ALBUM_ID"), r.getString("SONGTITLE"),
				r.getString("SONGARTIST"), r.getInt("TRACK"), r.getInt("YEAR"), r.getLong("SIZE"),
				r.getBoolean("TAGSMODIFIED"));
	}

}
