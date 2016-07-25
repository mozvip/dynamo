package dynamo.tvshows.jdbi;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
import dynamo.jdbi.UnrecognizedFileMapper;
import dynamo.jdbi.UnrecognizedFolderMapper;
import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.BindPath;
import dynamo.jdbi.core.BindStringList;
import dynamo.jdbi.core.BindUpper;
import dynamo.jdbi.core.DAO;
import dynamo.model.DownloadableStatus;
import dynamo.model.tvshows.TVShowSeason;
import model.ManagedEpisode;
import model.ManagedSeries;
import model.UnrecognizedFile;
import model.UnrecognizedFolder;

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

	@SqlUpdate("MERGE INTO MANAGEDSERIES"
			+ "(ID, NAME, IMDBID, BANNER, POSTER, NETWORK, FOLDER, ORIGINAL_LANGUAGE, METADATALANGUAGE, AUDIOLANGUAGE, SUBTITLELANGUAGE, ENDED, USEABSOLUTENUMBERING, AUTODOWNLOAD, BLACKLIST, AKA, QUALITIES) VALUES"
			+ "(:id, :name, :imdbId, :banner, :poster, :network, :folder, :originalLanguage, :metaDataLanguage, :audioLanguage, :subtitleLanguage, :ended, :useAbsoluteNumbering, :autoDownload, :blackList, :aka, :qualities )")
	public void saveTVShow(
			@Bind("id") String id,
			@Bind("name") String name,
			@Bind("imdbId") String imdbId,
			@Bind("banner") String banner,
			@Bind("poster") String poster,
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

	@SqlQuery("SELECT * FROM UNRECOGNIZEDFOLDER ORDER BY PATH")
	@Mapper(UnrecognizedFolderMapper.class)
	public List<UnrecognizedFolder> getUnrecognizedFolders();

	@SqlUpdate("DELETE FROM UNRECOGNIZEDFILE WHERE PATH=:path")
	public void deleteUnrecognizedFile(@BindPath("path") Path path);

	@SqlUpdate("DELETE FROM UNRECOGNIZEDFOLDER WHERE PATH=:path")
	public void deleteUnrecognizedFolder(@BindPath("path") Path path);

	@SqlUpdate("DELETE FROM UNRECOGNIZEDFILE WHERE SERIES_ID=:seriesId")
	public void deleteUnrecognizedFiles(@Bind("seriesId") String seriesId);

	@SqlUpdate("DELETE FROM MANAGEDSERIES WHERE ID=:seriesId")
	public void deleteTVShow(@Bind("seriesId") String seriesId);

	@SqlQuery("SELECT * FROM UNRECOGNIZEDFOLDER WHERE PATH=:path")
	@Mapper(UnrecognizedFolderMapper.class)
	public UnrecognizedFolder findUnrecognizedFolder(@BindPath("path") Path p);

	@SqlUpdate("INSERT INTO UNRECOGNIZEDFOLDER(PATH) VALUES(:path)")
	public void createUnrecognizedFolder(@BindPath("path") Path path);

	@SqlUpdate("INSERT INTO UNRECOGNIZEDFILE(PATH, SERIES_ID) VALUES(:path, :seriesId)")
	public void createUnrecognizedFile(@BindPath("path") Path path, @Bind("seriesId") String seriesId);

	@SqlQuery("SELECT * FROM UNRECOGNIZEDFILE WHERE SERIES_ID=:seriesId")
	@Mapper(UnrecognizedFileMapper.class)
	public List<UnrecognizedFile> getUnrecognizedFiles(@Bind("seriesId") String seriesId);

}
