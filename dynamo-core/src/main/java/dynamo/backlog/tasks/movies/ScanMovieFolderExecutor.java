package dynamo.backlog.tasks.movies;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.MovieDb;

import dynamo.backlog.tasks.core.AbstractNewFolderExecutor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.core.Language;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableDAO;
import dynamo.jdbi.MovieDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.subtitles.FindMovieSubtitleTask;
import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;
import dynamo.parsers.MovieInfo;
import dynamo.parsers.VideoNameParser;
import dynamo.suggesters.movies.IMDBTitle;
import dynamo.suggesters.movies.IMDBWatchListSuggester;
import dynamo.video.VideoManager;

public class ScanMovieFolderExecutor extends AbstractNewFolderExecutor<ScanMovieFolderTask> {
	
	private Set<String> imdbIds = new HashSet<String>();

	private DownloadableDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableDAO.class );
	private MovieDAO movieDAO = DAOManager.getInstance().getDAO( MovieDAO.class );

	private Map<Path, Movie> moviesMap;
	private Language subtitlesLanguage;

	public ScanMovieFolderExecutor( ScanMovieFolderTask item ) {
		super(item);
		moviesMap = new HashMap<Path, Movie>();

		List<Movie> allDownloadedMovies = movieDAO.findDownloaded();

		for (Movie movie : allDownloadedMovies) {
			Path moviePath = movie.getPath();
			if (moviePath.startsWith( item.getFolder() )) {
				moviesMap.put( moviePath, movie );
			}
		}
		subtitlesLanguage = MovieManager.getInstance().getSubtitlesLanguage();
	}
	
	public void parseFolder( Path folder ) throws IOException {
		DirectoryStream<Path> ds = Files.newDirectoryStream(folder, VideoFileFilter.getInstance());
		for (Path p : ds) {
			if (Files.isDirectory(p)) {
				parseFolder(p);
			} else {
				try {
					handleFile( p );
				} catch (IOException | InterruptedException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
			}
		}
	}
	
	public void handleFile( Path p ) throws IOException, InterruptedException {
		Path moviePath = p.toAbsolutePath();
		Movie movie = moviesMap.get( moviePath );

		if (movie != null) {
			
			if ( movie.getImdbID() != null && imdbIds.contains( movie.getImdbID()) ) {
				// duplicate
				downloadableDAO.delete( movie.getId() );
			} else {
				
				VideoManager.getInstance().getMetaData( movie, p );

				// fix imdb data if not set
				if ( (movie.getRating() <= 0 || movie.getYear() <= 0) && StringUtils.isNotBlank(movie.getImdbID())) {
					IMDBTitle imdbInfo = IMDBWatchListSuggester.extractIMDBTitle( movie.getImdbID() );
					if (imdbInfo != null) {
						movie.setRating( imdbInfo.getRating() );
						movie.setYear( imdbInfo.getYear() );
					}
				}

				// fix movieDB data if not set
				if (movie.getMovieDbId() == 0 || movie.getYear() <= 0 ) {
					MovieInfo movieInfo = VideoNameParser.getMovieInfo( p );
					if ( movieInfo != null ) {
						try {
							MovieManager.getInstance().setMovieInfo( movie, movieInfo );
						} catch (MovieDbException e) {
							ErrorManager.getInstance().reportThrowable( e );
						}
					}
				}

				boolean mustRefresh = false;
				if ( movie.getCoverImage() != null && LocalImageCache.getInstance().missFile( movie.getCoverImage() )) {
					mustRefresh = true;
				}
				if ( (movie.getImdbID() == null || movie.getCoverImage() == null) && movie.getMovieDbId() > 0 ) {
					mustRefresh = true;
				}
				
				if (mustRefresh) {
					try {
						MovieDb movieDb = MovieManager.getInstance().getMovieInfo( movie.getMovieDbId() );
						MovieManager.getInstance().associate(movie, movieDb);
					} catch (MovieDbException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}							
				}

				MovieManager.getInstance().save( movie );
			}

		} else {
			
			long downloadableId = downloadableDAO.createDownloadable( Movie.class, moviePath, null, DownloadableStatus.DOWNLOADED );
			movie = new Movie(
					downloadableId, DownloadableStatus.DOWNLOADED, moviePath, null, null, p.getFileName().toString(), null, false, null, null, null, null, null, null, null, -1, null, null, -1, -1, false );

			DownloadableManager.getInstance().addFile(downloadableId, p, 0);

			MovieInfo movieInfo = VideoNameParser.getMovieInfo( p );
			if ( movieInfo != null ) {
				try {
					MovieManager.getInstance().setMovieInfo( movie, movieInfo );
				} catch (MovieDbException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
			}
			
			if (movie.getImdbID() != null) {
				IMDBTitle imdbInfo = IMDBWatchListSuggester.extractIMDBTitle( movie.getImdbID() );
				if (imdbInfo != null) {
					movie.setRating( imdbInfo.getRating() );
					movie.setYear( imdbInfo.getYear() );
				}
			}

			MovieManager.getInstance().save( movie );
			VideoManager.getInstance().getMetaData( movie, p );
			
			moviesMap.put( moviePath, movie );
		}

		if (!movie.isSubtitled() && subtitlesLanguage != null) {
			queue( new FindMovieSubtitleTask( movie, subtitlesLanguage ), false );
		}
		
		imdbIds.add( movie.getImdbID() );		
	}

	@Override
	public void parsePath(Path p) throws IOException {
		if (Files.isDirectory(p)) {
			parseFolder(p);
		} else {
			try {
				handleFile( p );
			} catch (InterruptedException | IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
	}

	@Override
	public Filter<Path> getFileFilter() {
		return VideoFileFilter.getInstance();
	}

}
