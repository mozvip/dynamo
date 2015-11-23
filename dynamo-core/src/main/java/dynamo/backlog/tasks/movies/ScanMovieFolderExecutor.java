package dynamo.backlog.tasks.movies;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.MovieDb;

import dynamo.backlog.tasks.core.AbstractNewFolderExecutor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.core.Language;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.DownloadableFile;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.Downloadable;
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

	private Language subtitlesLanguage;

	public ScanMovieFolderExecutor( ScanMovieFolderTask item ) {
		super(item);
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

		DownloadableFile file = DownloadableManager.getInstance().getFile( p );
		
		Movie movie = null;

		if (file != null) {
			
			Downloadable downloadable = DownloadableFactory.getInstance().createInstance( file.getDownloadableId() );
			if (!(downloadable instanceof Movie)) {
				return;
			}
			
			movie = (Movie) downloadable;

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
			try {
				movie = handleNewMovie(p);
			} catch (MovieDbException | ParseException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}

		if (!movie.isSubtitled() && subtitlesLanguage != null) {
			queue( new FindMovieSubtitleTask( movie, subtitlesLanguage ), false );
		}

		imdbIds.add( movie.getImdbID() );		
	}

	protected Movie handleNewMovie( Path movieFile ) throws IOException, InterruptedException, MovieDbException, ParseException {

		Movie movie = null;
		MovieInfo movieInfo = VideoNameParser.getMovieInfo( movieFile );
		MovieDb movieDb = null;
		if (movieInfo != null) {
			movieDb = MovieManager.getInstance().searchByName( movieInfo.getName(), movieInfo.getYear(), null, false);
			if (movieDb != null) {
				if (movieDb.getImdbID() != null) {
					movie = MovieManager.getInstance().findByImdbId( movieDb.getImdbID() );
				}
			}
		}
		if (movie != null) {
			if (movie.getStatus() != DownloadableStatus.DOWNLOADED) {
				DownloadableManager.getInstance().updateStatus( movie, DownloadableStatus.DOWNLOADED);
			}
		} else {
			// create new movie
			String movieName = movieDb != null ? movieDb.getTitle() : movieFile.getFileName().toString();
			
			long id = downloadableDAO.createDownloadable( Movie.class, movieFile, null, DownloadableStatus.DOWNLOADED );
			movie = new Movie(
					id, DownloadableStatus.DOWNLOADED, null, null, movieName, null, false, null, null, null, null, null, null, null, -1, null, null, -1, -1, false );
			if ( movieInfo != null ) {
				try {
					MovieManager.getInstance().setMovieInfo( movie, movieInfo );
				} catch (MovieDbException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
			}
		}
		DownloadableManager.getInstance().addFile(movie.getId(), movieFile, 0);

		if (movie.getRating() <= 0 || movie.getYear() <=0 && movie.getImdbID() != null ) {
			IMDBTitle imdbInfo = IMDBWatchListSuggester.extractIMDBTitle( movie.getImdbID() );
			if (imdbInfo != null) {
				movie.setRating( imdbInfo.getRating() );
				movie.setYear( imdbInfo.getYear() );
			}
		}

		MovieManager.getInstance().save( movie );
		VideoManager.getInstance().getMetaData( movie, movieFile );
		return movie;
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
