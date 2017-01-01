package dynamo.core.model.video;

import java.util.Locale;
import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.jdbi.core.BindLocales;
import dynamo.jdbi.core.DAO;

@DAO(databaseId="dynamo")
public interface VideoDAO {
	
	@SqlUpdate("MERGE INTO VIDEO_METADATA(VIDEO_ID, AUDIO_LANGUAGES, SUBTITLE_LANGUAGES, WIDTH, HEIGHT, OPENSUBTITLES_HASH) VALUES(:videoId, :audioLanguages, :subtitleLanguages, :width, :height, :openSubtitlesHash)")
	public void saveMetaData(@Bind("videoId") long videoId, @BindLocales("audioLanguages") Set<Locale> audioLanguages, @BindLocales("subtitleLanguages") Set<Locale> subtitleLanguages, @Bind("width") int width, @Bind("height") int height, @Bind("openSubtitlesHash") String openSubtitlesHash);

	@SqlQuery("SELECT * FROM VIDEO_METADATA WHERE VIDEO_ID = :videoId")
	@Mapper(VideoMetaDataMapper.class)
	public VideoMetaData getMetaData( @Bind("videoId") long videoId );

}

