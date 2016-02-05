package dynamo.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.model.DownloadableStatus;
import dynamo.model.tvshows.TVShowSeason;

public class TVShowSeasonMapper implements ResultSetMapper<TVShowSeason> {

	@Override
	public TVShowSeason map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		
		String statusStr = r.getString("STATUS");
		DownloadableStatus status = statusStr != null ? DownloadableStatus.valueOf( statusStr ) : DownloadableStatus.IGNORED;
		
		return new TVShowSeason(
				r.getLong("ID"),
				status,
				r.getString("SERIES_ID"),
				r.getString("NAME"),
				r.getInt("SEASON")
		);
	}

}
