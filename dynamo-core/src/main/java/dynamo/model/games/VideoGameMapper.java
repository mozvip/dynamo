package dynamo.model.games;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.core.model.MapperUtils;
import dynamo.model.DownloadableStatus;

public class VideoGameMapper implements ResultSetMapper<VideoGame> {

	@Override
	public VideoGame map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return new VideoGame(
				r.getLong("ID"),
				MapperUtils.getEnum(r, "STATUS", DownloadableStatus.class),
				r.getString("COVER_IMAGE"),
				r.getString("NAME"),
				MapperUtils.getEnum(r, "PLATFORM", GamePlatform.class),
				r.getLong("THEGAMESDB_ID")
				);
	}

}
