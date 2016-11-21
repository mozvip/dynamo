package dynamo.tvshows.jdbi;

import java.time.LocalDate;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
import dynamo.core.model.DownloadableDAO;
import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.BindLocalDate;
import dynamo.model.DownloadableStatus;
import dynamo.tvshows.model.ManagedEpisode;

public interface ManagedEpisodeDAO extends DownloadableDAO<ManagedEpisode>{
	
	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.*, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE MANAGEDEPISODE.ID = :episodeId")
	@Mapper(ManagedEpisodeMapper.class)
	public ManagedEpisode find(@Bind("episodeId") long episodeId);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.*, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE DOWNLOADABLE.STATUS = :status")
	@Mapper(ManagedEpisodeMapper.class)
	public List<ManagedEpisode> findByStatus(@BindEnum("status") DownloadableStatus status);

	@SqlUpdate("MERGE INTO MANAGEDEPISODE ("
			+ "ID, EPISODENUMBER, FIRSTAIRED, QUALITY, RELEASEGROUP, SOURCE, WATCHED, SEASON_ID) "
			+ "KEY(SEASON_ID, EPISODENUMBER) VALUES ("
			+ ":episodeId, :episodeNumber, :firstAired, :videoQuality, :releaseGroup, :videoSource, :watched, :seasonId)")
	public void saveEpisode(
			@Bind("episodeId") long episodeId,
			@Bind("episodeNumber")int episodeNumber,
			@BindLocalDate("firstAired") LocalDate firstAired,
			@BindEnum("videoQuality") VideoQuality videoQuality,
			@Bind("releaseGroup") String releaseGroup,
			@BindEnum("videoSource") VideoSource videoSource,
			@Bind("watched") boolean watched,
			@Bind("seasonId") long seasonId
			);
	
	@SqlUpdate("UPDATE MANAGEDEPISODE SET WATCHED = true WHERE ID = :episodeId")
	public void setWatched(@Bind("episodeId") long episodeId);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.*, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE SEASON_ID = :seasonId")
	@Mapper(ManagedEpisodeMapper.class)
	public List<ManagedEpisode> findEpisodesForSeason(@Bind("seasonId") long seasonId);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.*, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE SEASON_ID = :seasonId AND EPISODENUMBER = :episodeNumber")
	@Mapper(ManagedEpisodeMapper.class)
	public ManagedEpisode findEpisode(@Bind("seasonId") long seasonId, @Bind("episodeNumber") int episodeNumber);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.*, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE TVSHOWSEASON.SERIES_ID = :seriesId AND DOWNLOADABLE.STATUS = :status")
	@Mapper(ManagedEpisodeMapper.class)
	public List<ManagedEpisode> findEpisodesForTVShowAndStatus(@Bind("seriesId") String seriesId, @BindEnum("status") DownloadableStatus status);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.*, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE TVSHOWSEASON.SERIES_ID = :seriesId")
	@Mapper(ManagedEpisodeMapper.class)
	public List<ManagedEpisode> findEpisodesForTVShow(@Bind("seriesId") String seriesId);

	@SqlQuery("SELECT MANAGEDEPISODE.*, DOWNLOADABLE.*, TVSHOWSEASON.SERIES_ID, TVSHOWSEASON.SEASON, MANAGEDSERIES.NAME AS SERIESNAME FROM MANAGEDEPISODE INNER JOIN TVSHOWSEASON ON MANAGEDEPISODE.SEASON_ID = TVSHOWSEASON.ID INNER JOIN DOWNLOADABLE ON MANAGEDEPISODE.ID = DOWNLOADABLE.ID INNER JOIN MANAGEDSERIES ON MANAGEDSERIES.ID = TVSHOWSEASON.SERIES_ID WHERE TVSHOWSEASON.SERIES_ID = :seriesId AND TVSHOWSEASON.SEASON=:seasonNumber")
	@Mapper(ManagedEpisodeMapper.class)
	public List<ManagedEpisode> findEpisodesForTVShowAndSeason(@Bind("seriesId") String seriesId, @Bind("seasonNumber") int seasonNumber);
	

}
