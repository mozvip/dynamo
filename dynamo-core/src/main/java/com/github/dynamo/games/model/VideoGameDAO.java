package com.github.dynamo.games.model;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import com.github.dynamo.core.model.DownloadableDAO;
import com.github.dynamo.jdbi.core.BindEnum;
import com.github.dynamo.model.DownloadableStatus;

public interface VideoGameDAO extends DownloadableDAO<VideoGame> {
	
	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID WHERE VIDEOGAME.ID = :videoGameId")
	@Mapper(VideoGameMapper.class)
	public VideoGame find(@Bind("videoGameId") long videoGameId);

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID AND DOWNLOADABLE.STATUS = :status")
	@Mapper(VideoGameMapper.class)
	public List<VideoGame> findByStatus( @BindEnum("status") DownloadableStatus status );

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID AND DOWNLOADABLE.STATUS = :status AND PLATFORM = :platform")
	@Mapper(VideoGameMapper.class)
	public List<VideoGame> findAll( @BindEnum("status") DownloadableStatus status, @BindEnum("platform") GamePlatform platform );

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID AND ( DOWNLOADABLE.STATUS = :status1 OR DOWNLOADABLE.STATUS = :status2 ) AND PLATFORM = :platform")
	@Mapper(VideoGameMapper.class)
	public List<VideoGame> findAll( @BindEnum("status1") DownloadableStatus status1, @BindEnum("status2") DownloadableStatus status2, @BindEnum("platform") GamePlatform platform );

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID AND ( DOWNLOADABLE.STATUS = :status1 OR DOWNLOADABLE.STATUS = :status2 )")
	@Mapper(VideoGameMapper.class)
	public List<VideoGame> findAll( @BindEnum("status1") DownloadableStatus status1, @BindEnum("status2") DownloadableStatus status2 );

	@SqlUpdate("MERGE INTO VIDEOGAME(ID, PLATFORM, THEGAMESDB_ID) VALUES (:id, :platform, :theGamesDBId)")
	void save(@Bind("id") long id, @BindEnum("platform") GamePlatform platform, @Bind("theGamesDBId") Long theGamesDBId);

	@SqlQuery("SELECT DOWNLOADABLE.*, VIDEOGAME.* FROM VIDEOGAME INNER JOIN DOWNLOADABLE ON VIDEOGAME.ID = DOWNLOADABLE.ID WHERE VIDEOGAME.THEGAMESDB_ID = :theGamesDbId")
	@Mapper(VideoGameMapper.class)
	public VideoGame findByTheGamesDbId(@Bind("theGamesDbId") long theGamesDbId);

}
