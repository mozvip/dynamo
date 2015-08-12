package dynamo.model.games;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.DAO;
import dynamo.model.DownloadableStatus;

@DAO(databaseId="dynamo")
public interface VideoGameDAO {
	
	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID WHERE VIDEOGAME.ID = :videoGameId")
	@Mapper(VideoGameMapper.class)
	public VideoGame find(@Bind("videoGameId") long videoGameId);

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID")
	@Mapper(VideoGameMapper.class)
	public List<VideoGame> findAll();

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID AND DOWNLOADABLE.STATUS = :status")
	@Mapper(VideoGameMapper.class)
	public List<VideoGame> findAll( @BindEnum("status") DownloadableStatus status );

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID AND DOWNLOADABLE.STATUS = :status AND PLATFORM = :platform")
	@Mapper(VideoGameMapper.class)
	public List<VideoGame> findAll( @BindEnum("status") DownloadableStatus status, @BindEnum("platform") GamePlatform platform );

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID AND DOWNLOADABLE.STATUS IN ('WANTED', 'SNATCHED')")
	@Mapper(VideoGameMapper.class)
	public List<VideoGame> findWantedAndSnatched();

	@SqlUpdate("MERGE INTO VIDEOGAME(ID, NAME, PLATFORM, THEGAMESDB_ID) VALUES (:id, :name, :platform, :theGamesDBId)")
	void save(@Bind("id") long id, @Bind("name") String name, @BindEnum("platform") GamePlatform platform, @Bind("theGamesDBId") Long theGamesDBId);

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID WHERE VIDEOGAME.THEGAMESDB_ID = :theGamesDbId")
	@Mapper(VideoGameMapper.class)
	public VideoGame findByTheGamesDbId(@Bind("theGamesDbId") long theGamesDbId);

}
