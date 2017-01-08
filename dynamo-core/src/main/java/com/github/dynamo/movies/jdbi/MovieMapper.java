package com.github.dynamo.movies.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.VideoSource;
import com.github.dynamo.core.model.MapperUtils;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.movies.model.Movie;

public class MovieMapper implements ResultSetMapper<Movie> {

	@Override
	public Movie map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {

		return new Movie(
				r.getLong("ID"),
				MapperUtils.getEnum(r, "STATUS", DownloadableStatus.class),
				r.getString("AKA"),
				r.getString("NAME"),
				r.getString("LABEL"),
				MapperUtils.getEnum(r, "WANTEDQUALITY", VideoQuality.class),
				MapperUtils.getEnum(r, "WANTEDAUDIOLANGUAGE", Language.class),
				MapperUtils.getEnum(r, "WANTEDSUBTITLESLANGUAGE", Language.class),
				MapperUtils.getEnum(r, "ORIGINALLANGUAGE", Language.class),
				MapperUtils.getEnum(r, "QUALITY", VideoQuality.class),
				MapperUtils.getEnum(r, "SOURCE", VideoSource.class),
				r.getString("RELEASEGROUP"),
				r.getInt("MOVIEDBID"),
				r.getString("IMDBID"),
				r.getString("TRAKTURL"),
				r.getFloat("RATING"),
				r.getInt("YEAR"),
				r.getBoolean("WATCHED")
		);
	}

}
