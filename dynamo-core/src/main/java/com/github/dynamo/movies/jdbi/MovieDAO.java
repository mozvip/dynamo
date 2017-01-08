package com.github.dynamo.movies.jdbi;

import java.nio.file.Path;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.VideoSource;
import com.github.dynamo.core.model.DownloadableDAO;
import com.github.dynamo.jdbi.core.BindEnum;
import com.github.dynamo.jdbi.core.BindPath;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.movies.model.Movie;

public interface MovieDAO extends DownloadableDAO<Movie>{

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE MOVIE.ID = :movieId")
	@Mapper(MovieMapper.class)
	Movie find(@Bind("movieId") long movieId);

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = :status ORDER BY NAME")
	@Mapper(MovieMapper.class)
	List<Movie> findByStatus( @BindEnum("status") DownloadableStatus status );

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE MOVIEDBID = :movieDbId")
	@Mapper(MovieMapper.class)
	Movie findByMovieDbId(@Bind("movieDbId") int movieDbId);

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE IMDBID = :imdbId")
	@Mapper(MovieMapper.class)
	Movie findByImdbId(@Bind("imdbId") String imdbId);

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = 'DOWNLOADED' ORDER BY NAME")
	@Mapper(MovieMapper.class)
	List<Movie> findDownloaded();

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = 'SUGGESTED' ORDER BY NAME")
	@Mapper(MovieMapper.class)
	List<Movie> findSuggested();

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS IN('WANTED','SNATCHED') ORDER BY NAME")
	@Mapper(MovieMapper.class)
	List<Movie> findWanted();

	@SqlQuery("SELECT DOWNLOADABLE.*, MOVIE.* FROM MOVIE INNER JOIN DOWNLOADABLE ON MOVIE.ID=DOWNLOADABLE.ID ORDER BY NAME")
	@Mapper(MovieMapper.class)
	List<Movie> find();

	@SqlUpdate("MERGE INTO MOVIE("
			+ "ID, IMDBID, MOVIEDBID, ORIGINALLANGUAGE, QUALITY, RATING, RELEASEGROUP, SOURCE, TRAKTURL, WANTEDAUDIOLANGUAGE, WANTEDSUBTITLESLANGUAGE, WANTEDQUALITY, WATCHED) VALUES("
			+ ":moviedId, :imdbId, :movieDbId, :originalLanguage, :quality, :rating, :releasegroup, :videoSource, :trakURL, :wantedAudioLanguage, :wantedSubtitlesLanguage, :wantedQuality, :watched)")
	void save(@Bind("moviedId") long movieId, @Bind("imdbId") String imdbId, @Bind("movieDbId") int movieDbId, @BindEnum("originalLanguage") Language originalLanguage,
			@BindEnum("quality") VideoQuality quality, @Bind("rating") Float rating, @Bind("releasegroup") String releasegroup, @BindEnum("videoSource") VideoSource videoSource,
			@Bind("trakURL") String trakURL, @BindEnum("wantedAudioLanguage") Language wantedAudioLanguage,
			@BindEnum("wantedSubtitlesLanguage") Language wantedSubtitlesLanguage, @BindEnum("wantedQuality") VideoQuality wantedQuality, @Bind("watched") boolean watched	);

	@SqlUpdate("UPDATE MOVIE SET SUBTITLED = true, SUBTITLESPATH=:subTitlesPath WHERE ID = :movieId")
	public void setSubtitled(@Bind("movieId") long movieId, @BindPath("subTitlesPath") Path subTitlesPath);

}
