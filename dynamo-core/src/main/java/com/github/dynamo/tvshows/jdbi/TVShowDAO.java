package com.github.dynamo.tvshows.jdbi;

import java.nio.file.Path;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.jdbi.core.BindEnum;
import com.github.dynamo.jdbi.core.BindPath;
import com.github.dynamo.jdbi.core.BindStringList;
import com.github.dynamo.jdbi.core.BindUpper;
import com.github.dynamo.jdbi.core.DAO;
import com.github.dynamo.tvshows.model.ManagedSeries;

@DAO(databaseId="dynamo")
public interface TVShowDAO {

	@SqlQuery("SELECT * FROM MANAGEDSERIES ORDER BY NAME")
	@Mapper(ManagedSeriesMapper.class)
	public List<ManagedSeries> getTVShows();

	@SqlQuery("SELECT * FROM MANAGEDSERIES WHERE MANAGEDSERIES.ID = :tvShowId")
	@Mapper(ManagedSeriesMapper.class)
	public List<ManagedSeries> getTVShow(@Bind("tvShowId") long tvShowId);

	@SqlQuery("SELECT * FROM MANAGEDSERIES WHERE UPPER(NAME) = :name OR INSTR(:name, UPPER(AKA)) > 0")
	@Mapper(ManagedSeriesMapper.class)
	public ManagedSeries findTVShowByName(@BindUpper("name") String name);

	@SqlQuery("SELECT * FROM MANAGEDSERIES WHERE FOLDER = :folder")
	@Mapper(ManagedSeriesMapper.class)
	public ManagedSeries getTVShowForFolder( @BindPath("folder") Path folder );

	@SqlQuery("SELECT * FROM MANAGEDSERIES WHERE ID = :id")
	@Mapper(ManagedSeriesMapper.class)
	public ManagedSeries findTVShow(@Bind("id") String id);
	
	@SqlQuery("SELECT * FROM MANAGEDSERIES WHERE IMDBID = :imdbId")
	@Mapper(ManagedSeriesMapper.class)
	public ManagedSeries findTVShowByImdbId(@Bind("imdbId") String imdbId);
	
	@SqlUpdate("DELETE FROM MANAGEDSERIES WHERE ID=:seriesId")
	public void deleteTVShow(@Bind("seriesId") String seriesId);

	@SqlUpdate("MERGE INTO MANAGEDSERIES"
			+ "(ID, NAME, IMDBID, NETWORK, FOLDER, ORIGINAL_LANGUAGE, METADATALANGUAGE, AUDIOLANGUAGE, SUBTITLELANGUAGE, ENDED, USEABSOLUTENUMBERING, AUTODOWNLOAD, BLACKLIST, AKA, QUALITIES) VALUES"
			+ "(:id, :name, :imdbId, :network, :folder, :originalLanguage, :metaDataLanguage, :audioLanguage, :subtitleLanguage, :ended, :useAbsoluteNumbering, :autoDownload, :blackList, :aka, :qualities )")
	public void saveTVShow(
			@Bind("id") String id,
			@Bind("name") String name,
			@Bind("imdbId") String imdbId,
			@Bind("network") String network,
			@BindPath("folder") Path folder,
			@BindEnum("originalLanguage") Language originalLanguage,
			@BindEnum("metaDataLanguage") Language metaDataLanguage,
			@BindEnum("audioLanguage") Language audioLanguage,
			@BindEnum("subtitleLanguage") Language subtitleLanguage,
			@Bind("ended") boolean ended,
			@Bind("useAbsoluteNumbering") boolean useAbsoluteNumbering,
			@Bind("autoDownload") boolean autoDownload,
			@BindStringList("blackList") List<String> blackList,
			@BindStringList("aka") List<String> aka,
			@BindStringList("qualities") List<VideoQuality> qualities);

	@SqlUpdate("UPDATE MANAGEDSERIES SET AUTODOWNLOAD = NOT AUTODOWNLOAD WHERE ID=:seriesId")
	public void toggleAutoDownload(@Bind("seriesId") String seriesId);

}
