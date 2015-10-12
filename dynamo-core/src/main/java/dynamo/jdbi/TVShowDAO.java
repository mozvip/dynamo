package dynamo.jdbi;

import java.nio.file.Path;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
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
			+ "(ID, NAME, IMDBID, LASTUPDATEDDATE, BANNER, POSTER, NETWORK, FOLDER, ORIGINAL_LANGUAGE, METADATALANGUAGE, AUDIOLANGUAGE, SUBTITLELANGUAGE, ENDED, DOWNLOADED, MISSING, USEABSOLUTENUMBERING, AUTODOWNLOAD, BLACKLIST, AKA, QUALITIES) VALUES"
			+ "(:id, :name, :imdbId, CURRENT_TIMESTAMP(), :banner, :poster, :network, :folder, :originalLanguage, :metaDataLanguage, :audioLanguage, :subtitleLanguage, :ended, :downloaded, :missing, :useAbsoluteNumbering, :autoDownload, :blackList, :aka, :qualities )")
	public void saveTVShow(
			@BindBean ManagedSeries managedSeries,
			@BindEnum("metaDataLanguage") Language metaDataLanguage, @BindEnum("originalLanguage") Language originalLanguage, @BindEnum("audioLanguage") Language audioLanguage, @BindEnum("subtitleLanguage") Language subtitleLanguage,
			@BindPath("folder") Path folder, @BindStringList("blackList") List<String> blackList, @BindStringList("aka") List<String> aka, @BindStringList("qualities") List<VideoQuality> qualities);

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

	@SqlUpdate("UPDATE MANAGEDEPISODE SET SUBTITLED = true, SUBTITLESPATH=:path WHERE ID = :episodeId")
	public void setSubtitled(@Bind("episodeId") long episodeId, @BindPath("path") Path path);

	@SqlUpdate("UPDATE MANAGEDEPISODE SET SUBTITLED = true WHERE ID = :episodeId")
	public void setSubtitled(@Bind("episodeId") long episodeId);

	@SqlUpdate("UPDATE MANAGEDEPISODE SET SUBTITLESPATH = :path, SUBTITLED = true WHERE ID = :episodeId")
	public void updateSubtitlesPath( @Bind("episodeId") long episodeId, @BindPath("path") Path path );

	@SqlUpdate("INSERT INTO TVSHOWSEASON (ID, SERIES_ID, SEASON) VALUES( :id, :seriesId, :season )")
	public void createSeason( @Bind("id") long id, @Bind("seriesId") String seriesId, @Bind("season") int seasonNumber );

	@SqlQuery("SELECT * FROM UNRECOGNIZEDFILE WHERE SERIES_ID=:seriesId")
	@Mapper(UnrecognizedFileMapper.class)
	public List<UnrecognizedFile> getUnrecognizedFiles(@Bind("seriesId") String seriesId);

	@SqlQuery("SELECT TVSHOWSEASON.*, MANAGEDSERIES.NAME, DOWNLOADABLE.STATUS, DOWNLOADABLE.PATH FROM TVSHOWSEASON INNER JOIN MANAGEDSERIES ON TVSHOWSEASON.SERIES_ID = MANAGEDSERIES.ID INNER JOIN DOWNLOADABLE ON TVSHOWSEASON.ID = DOWNLOADABLE.ID WHERE SERIES_ID = :seriesId ORDER BY SEASON DESC")
	@Mapper(TVShowSeasonMapper.class)
	public List<TVShowSeason> findSeasons( @Bind("seriesId") String seriesId );

	@SqlQuery("SELECT TVSHOWSEASON.*, MANAGEDSERIES.NAME, DOWNLOADABLE.STATUS, DOWNLOADABLE.PATH FROM TVSHOWSEASON INNER JOIN MANAGEDSERIES ON TVSHOWSEASON.SERIES_ID = MANAGEDSERIES.ID INNER JOIN DOWNLOADABLE ON TVSHOWSEASON.ID = DOWNLOADABLE.ID")
	@Mapper(TVShowSeasonMapper.class)
	public List<TVShowSeason> findSeasons();

	@SqlQuery("SELECT TVSHOWSEASON.*, MANAGEDSERIES.NAME, DOWNLOADABLE.STATUS, DOWNLOADABLE.PATH FROM TVSHOWSEASON INNER JOIN MANAGEDSERIES ON TVSHOWSEASON.SERIES_ID = MANAGEDSERIES.ID INNER JOIN DOWNLOADABLE ON TVSHOWSEASON.ID = DOWNLOADABLE.ID WHERE SERIES_ID = :seriesId AND SEASON = :seasonNumber")
	@Mapper(TVShowSeasonMapper.class)
	public TVShowSeason findSeason(@Bind("seriesId") String seriesId, @Bind("seasonNumber") int seasonNumber);

	@SqlQuery("SELECT TVSHOWSEASON.*, MANAGEDSERIES.NAME, DOWNLOADABLE.STATUS, DOWNLOADABLE.PATH FROM TVSHOWSEASON INNER JOIN MANAGEDSERIES ON TVSHOWSEASON.SERIES_ID = MANAGEDSERIES.ID INNER JOIN DOWNLOADABLE ON TVSHOWSEASON.ID = DOWNLOADABLE.ID WHERE TVSHOWSEASON.ID = :seasonId")
	@Mapper(TVShowSeasonMapper.class)
	public TVShowSeason findSeason(@Bind("seasonId") long seasonId);

	@SqlUpdate("MERGE INTO MANAGEDEPISODE ("
			+ "ID, EPISODENAME, EPISODENUMBER, FIRSTAIRED, QUALITY, RELEASEGROUP, SOURCE, SUBTITLED, SUBTITLESPATH, TVDBID, WATCHED, SEASON_ID) "
			+ "KEY(SEASON_ID, EPISODENUMBER) VALUES ("
			+ ":episode.id, :episode.episodeName, :episode.episodeNumber, :episode.firstAired, :videoQuality, :episode.releaseGroup, :videoSource, :episode.subtitled, :subtitlesPath, :episode.tvdbId, :episode.watched, :episode.seasonId)")
	public void saveEpisode(@BindBean("episode") ManagedEpisode episode, @BindEnum("videoQuality") VideoQuality videoQuality, @BindEnum("videoSource") VideoSource videoSource, @BindPath("subtitlesPath") Path subtitlesPath );

	@SqlUpdate("UPDATE MANAGEDEPISODE SET WATCHED = true WHERE ID = :episodeId")
	public void setWatched(@Bind("episodeId") long episodeId);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.PATH, DOWNLOADABLE.STATUS, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE SEASON_ID = :seasonId")
	@Mapper(ManagedEpisodeMapper.class)
	public List<ManagedEpisode> findEpisodesForSeason(@Bind("seasonId") long seasonId);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.PATH, DOWNLOADABLE.STATUS, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE MANAGEDEPISODE.ID = :episodeId")
	@Mapper(ManagedEpisodeMapper.class)
	public ManagedEpisode findEpisode(@Bind("episodeId") long episodeId);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.PATH, DOWNLOADABLE.STATUS, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE SEASON_ID = :seasonId AND EPISODENUMBER = :episodeNumber")
	@Mapper(ManagedEpisodeMapper.class)
	public ManagedEpisode findEpisode(@Bind("seasonId") long seasonId, @Bind("episodeNumber") int episodeNumber);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.PATH, DOWNLOADABLE.STATUS, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE TVSHOWSEASON.SERIES_ID = :seriesId AND DOWNLOADABLE.STATUS = :status")
	@Mapper(ManagedEpisodeMapper.class)
	public List<ManagedEpisode> findEpisodesForTVShowAndStatus(@Bind("seriesId") String seriesId, @BindEnum("status") DownloadableStatus status);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.PATH, DOWNLOADABLE.STATUS, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE TVSHOWSEASON.SERIES_ID = :seriesId")
	@Mapper(ManagedEpisodeMapper.class)
	public List<ManagedEpisode> findEpisodesForTVShow(@Bind("seriesId") String seriesId);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.PATH, DOWNLOADABLE.STATUS, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE TVSHOWSEASON.SERIES_ID = :seriesId AND TVSHOWSEASON.SEASON=:seasonNumber")
	@Mapper(ManagedEpisodeMapper.class)
	public List<ManagedEpisode> findEpisodesForTVShowAndSeason(@Bind("seriesId") String seriesId, @Bind("seasonNumber") int seasonNumber);

}
