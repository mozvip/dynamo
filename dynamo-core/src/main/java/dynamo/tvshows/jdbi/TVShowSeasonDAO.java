package dynamo.tvshows.jdbi;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.core.model.DownloadableDAO;
import dynamo.jdbi.core.BindEnum;
import dynamo.model.DownloadableStatus;
import dynamo.model.tvshows.TVShowSeason;

public interface TVShowSeasonDAO extends DownloadableDAO<TVShowSeason> {
	
	@SqlQuery("SELECT TVSHOWSEASON.*, DOWNLOADABLE.* FROM TVSHOWSEASON INNER JOIN DOWNLOADABLE ON TVSHOWSEASON.ID = DOWNLOADABLE.ID WHERE TVSHOWSEASON.ID = :seasonId")
	@Mapper(TVShowSeasonMapper.class)
	@Override
	public TVShowSeason find(@Bind("seasonId") long seasonId);

	@SqlQuery("SELECT TVSHOWSEASON.*, DOWNLOADABLE.* FROM TVSHOWSEASON INNER JOIN DOWNLOADABLE ON TVSHOWSEASON.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = :status")
	@Mapper(TVShowSeasonMapper.class)
	@Override
	public List<TVShowSeason> findByStatus( @BindEnum("status") DownloadableStatus status );

	@SqlUpdate("INSERT INTO TVSHOWSEASON (ID, SERIES_ID, SEASON) VALUES( :id, :seriesId, :season )")
	public void createSeason( @Bind("id") long id, @Bind("seriesId") String seriesId, @Bind("season") int seasonNumber );

	@SqlQuery("SELECT TVSHOWSEASON.*, DOWNLOADABLE.* FROM TVSHOWSEASON INNER JOIN DOWNLOADABLE ON TVSHOWSEASON.ID = DOWNLOADABLE.ID WHERE SERIES_ID = :seriesId ORDER BY SEASON DESC")
	@Mapper(TVShowSeasonMapper.class)
	public List<TVShowSeason> findSeasons( @Bind("seriesId") String seriesId );

	@SqlQuery("SELECT TVSHOWSEASON.*, DOWNLOADABLE.* FROM TVSHOWSEASON INNER JOIN DOWNLOADABLE ON TVSHOWSEASON.ID = DOWNLOADABLE.ID")
	@Mapper(TVShowSeasonMapper.class)
	public List<TVShowSeason> findSeasons();

	@SqlQuery("SELECT TVSHOWSEASON.*, DOWNLOADABLE.* FROM TVSHOWSEASON INNER JOIN DOWNLOADABLE ON TVSHOWSEASON.ID = DOWNLOADABLE.ID WHERE SERIES_ID = :seriesId AND SEASON = :seasonNumber")
	@Mapper(TVShowSeasonMapper.class)
	public TVShowSeason findSeason(@Bind("seriesId") String seriesId, @Bind("seasonNumber") int seasonNumber);
	

}
