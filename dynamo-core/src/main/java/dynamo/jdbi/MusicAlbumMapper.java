package dynamo.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.core.model.MapperUtils;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicQuality;

public class MusicAlbumMapper implements ResultSetMapper<MusicAlbum> {

	@Override
	public MusicAlbum map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {

		return new MusicAlbum(
				r.getLong("ID"),
				MapperUtils.getEnum(r, "STATUS", DownloadableStatus.class),
				MapperUtils.getPath(r, "PATH"),
				r.getString("COVER_IMAGE"),
				r.getString("AKA"),
				r.getString("ARTIST_NAME"),
				r.getString("ALBUM"),
				r.getString("GENRE"),
				MapperUtils.getEnum(r, "QUALITY", MusicQuality.class), r.getString("ALLMUSICURL"));
	}

}
