package dynamo.core.model.video;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.core.Language;
import dynamo.core.model.MapperUtils;

public class VideoMetaDataMapper implements ResultSetMapper<VideoMetaData> {

	@Override
	public VideoMetaData map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new VideoMetaData(
				(Set<Locale>) MapperUtils.getLocales(r.getString("AUDIO_LANGUAGES")),
				(Set<Locale>) MapperUtils.getLocales(r.getString("SUBTITLE_LANGUAGES")),
				r.getInt("WIDTH"),
				r.getInt("HEIGHT"),
				r.getString("OPENSUBTITLES_HASH")
		);
	}

}
