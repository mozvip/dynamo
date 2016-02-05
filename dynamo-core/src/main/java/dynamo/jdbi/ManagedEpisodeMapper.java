package dynamo.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
import dynamo.core.model.MapperUtils;
import dynamo.model.DownloadableStatus;
import model.ManagedEpisode;

public class ManagedEpisodeMapper implements ResultSetMapper<ManagedEpisode> {

	@Override
	public ManagedEpisode map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {

		return new ManagedEpisode(r.getLong("ID"),
				MapperUtils.getEnum(r, "STATUS",
				DownloadableStatus.class), r.getString("SERIESNAME"),
				MapperUtils.getPath(r, "SUBTITLESPATH"),
				MapperUtils.getEnum(r, "QUALITY", VideoQuality.class),
				MapperUtils.getEnum(r, "SOURCE", VideoSource.class),
				r.getString("RELEASEGROUP"), r.getString("SERIES_ID"),
				r.getLong("SEASON_ID"), r.getInt("SEASON"),
				r.getInt("EPISODENUMBER"), r.getInt("ABSOLUTENUMBER"),
				r.getString("NAME"), r.getDate("FIRSTAIRED"),
				r.getBoolean("SUBTITLED"), r.getBoolean("WATCHED"));
	}

}
