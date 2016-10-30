package dynamo.music.jdbi;

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
				r.getString("NAME"),
				r.getString("LABEL"),
				MapperUtils.getEnum(r, "STATUS", DownloadableStatus.class),
				r.getInt("YEAR"),
				r.getDate("CREATION_DATE"),
				MapperUtils.getPath(r, "FOLDER"),
				r.getString("AKA"),
				r.getString("ARTIST_NAME"),
				r.getString("GENRE"),
				MapperUtils.getEnum(r, "QUALITY", MusicQuality.class),
				r.getLong("TADB_ALBUM_ID")
				);
	}

}
