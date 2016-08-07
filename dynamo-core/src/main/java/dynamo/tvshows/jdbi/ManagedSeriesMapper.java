package dynamo.tvshows.jdbi;

import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.model.MapperUtils;
import model.ManagedSeries;

public class ManagedSeriesMapper implements ResultSetMapper<ManagedSeries> {

	@Override
	public ManagedSeries map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		
		List<VideoQuality> qualities = (List<VideoQuality>) MapperUtils.getEnumList( r.getString("QUALITIES"), VideoQuality.class );
		if (qualities.isEmpty()) {
			qualities.addAll( Arrays.asList(VideoQuality.values()) );
		}
		
		String subtitleLanguage = r.getString("SUBTITLELANGUAGE");
	
		ManagedSeries m = new ManagedSeries(r.getString("ID"),
				r.getString("NAME"), r.getString("IMDBID"),
				r.getString("NETWORK"), Paths.get(r.getString("FOLDER")),
				MapperUtils.getEnum(r, "ORIGINAL_LANGUAGE", Language.class),
				MapperUtils.getEnum(r, "METADATALANGUAGE", Language.class),
				MapperUtils.getEnum(r, "AUDIOLANGUAGE", Language.class),
				subtitleLanguage != null ? Language.valueOf(subtitleLanguage) : null,
				r.getBoolean("ENDED"),
				r.getBoolean("USEABSOLUTENUMBERING"),
				r.getBoolean("AUTODOWNLOAD"),
				MapperUtils.getStringList(r.getString("AKA")),
				qualities,
				MapperUtils.getStringList(r.getString("BLACKLIST")));

		return m;
	}

}
