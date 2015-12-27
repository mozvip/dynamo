package dynamo.jdbi;

import java.nio.file.Path;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.BindPath;
import dynamo.jdbi.core.DAO;
import dynamo.model.DownloadableStatus;
import dynamo.model.movies.Movie;

@DAO(databaseId="dynamo")
public interface MovieDAO {

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE MOVIEDBID = :movieDbId")
	@Mapper(MovieMapper.class)
	Movie findByMovieDbId(@Bind("movieDbId") int movieDbId);

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE IMDBID = :imdbId")
	@Mapper(MovieMapper.class)
	Movie findByImdbId(@Bind("imdbId") String imdbId);

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE MOVIE.ID = :movieId")
	@Mapper(MovieMapper.class)
	Movie find(@Bind("movieId") long movieId);

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = :status ORDER BY MOVIE.NAME")
	@Mapper(MovieMapper.class)
	List<Movie> findByStatus( @BindEnum("status") DownloadableStatus status );

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = 'DOWNLOADED' ORDER BY MOVIE.NAME")
	@Mapper(MovieMapper.class)
	List<Movie> findDownloaded();

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = 'SUGGESTED' ORDER BY MOVIE.NAME")
	@Mapper(MovieMapper.class)
	List<Movie> findSuggested();

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS IN('WANTED','SNATCHED') ORDER BY MOVIE.NAME")
	@Mapper(MovieMapper.class)
	List<Movie> findWanted();

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID ORDER BY MOVIE.NAME")
	@Mapper(MovieMapper.class)
	List<Movie> find();

	@SqlUpdate("MERGE INTO MOVIE("
			+ "ID, IMDBID, MOVIEDBID, NAME, YEAR, ORIGINALLANGUAGE, QUALITY, RATING, RELEASEGROUP, SOURCE, SUBTITLED, SUBTITLESPATH, TRAKTURL, WANTEDAUDIOLANGUAGE, WANTEDSUBTITLESLANGUAGE, WANTEDQUALITY, WATCHED) VALUES("
			+ ":moviedId, :imdbId, :movieDbId, :name, :year, :originalLanguage, :quality, :rating, :releasegroup, :videoSource, :subtitled, :subtitlesPath, :trakURL, :wantedAudioLanguage, :wantedSubtitlesLanguage, :wantedQuality, :watched)")
	void save(@Bind("moviedId") long movieId, @Bind("imdbId") String imdbId, @Bind("movieDbId") int movieDbId, @Bind("name") String name, @Bind("year") int year, @BindEnum("originalLanguage") Language originalLanguage,
			@BindEnum("quality") VideoQuality quality, @Bind("rating") Float rating, @Bind("releasegroup") String releasegroup, @BindEnum("videoSource") VideoSource videoSource,
			@Bind("subtitled") boolean subtitled, @BindPath("subtitlesPath") Path subtitlesPath, @Bind("trakURL") String trakURL, @BindEnum("wantedAudioLanguage") Language wantedAudioLanguage,
			@BindEnum("wantedSubtitlesLanguage") Language wantedSubtitlesLanguage, @BindEnum("wantedQuality") VideoQuality wantedQuality, @Bind("watched") boolean watched	);

	@SqlUpdate("UPDATE MOVIE SET SUBTITLED = true, SUBTITLESPATH=:subTitlesPath WHERE ID = :movieId")
	public void setSubtitled(@Bind("movieId") long movieId, @BindPath("subTitlesPath") Path subTitlesPath);

	@SqlUpdate("DELETE FROM MOVIE WHERE MOVIE.IMDBID=:imdbId AND (SELECT DOWNLOADABLE.STATUS FROM DOWNLOADABLE WHERE DOWNLOADABLE.ID = MOVIE.ID ) ='SUGGESTED'")
	void deleteIfSuggested(@Bind("imdbId") String imdbId);

}
