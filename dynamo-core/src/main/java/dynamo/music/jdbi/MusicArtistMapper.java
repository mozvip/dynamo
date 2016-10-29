package dynamo.music.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.model.music.MusicArtist;

public class MusicArtistMapper implements ResultSetMapper<MusicArtist> {

	@Override
	public MusicArtist map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return new MusicArtist(r.getString("NAME"), r.getBoolean("FAVORITE"), r.getBoolean("BLACKLISTED"), r.getLong("TADB_ARTIST_ID"), r.getString("AKA"));
	}

}
