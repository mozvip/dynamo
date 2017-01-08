package com.github.dynamo.tvshows.jdbi;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.VideoSource;
import com.github.dynamo.core.model.MapperUtils;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.tvshows.model.ManagedEpisode;

public class ManagedEpisodeMapper implements ResultSetMapper<ManagedEpisode> {

	@Override
	public ManagedEpisode map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {

		Date firstAiredDate = r.getDate("FIRSTAIRED");
		return new ManagedEpisode(r.getLong("ID"),
				MapperUtils.getEnum(r, "STATUS",
				DownloadableStatus.class), r.getString("SERIESNAME"),
				MapperUtils.getEnum(r, "QUALITY", VideoQuality.class),
				MapperUtils.getEnum(r, "SOURCE", VideoSource.class),
				r.getString("RELEASEGROUP"), r.getString("SERIES_ID"),
				r.getLong("SEASON_ID"),
				r.getInt("SEASON"),
				r.getInt("EPISODENUMBER"),
				r.getInt("ABSOLUTENUMBER"),
				r.getString("NAME"),
				firstAiredDate != null ? firstAiredDate.toLocalDate() : null,
				r.getBoolean("WATCHED"), r.getString("LABEL"));
	}

}
