package dynamo.backlog.tasks.movies;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.ScanFolderExecutor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.core.Language;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.subtitles.FindMovieSubtitleTask;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.parsers.ParsedMovieInfo;
import dynamo.parsers.VideoNameParser;
import dynamo.suggesters.movies.IMDBTitle;
import dynamo.suggesters.movies.IMDBWatchListSuggester;
import dynamo.video.VideoManager;

public class ScanMovieFolderExecutor extends ScanFolderExecutor<ScanMovieFolderTask> {
	
	private Set<String> imdbIds = new HashSet<String>();

	private DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );

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
				DownloadableManager.getInstance().delete( movie.getId() );
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
					ParsedMovieInfo movieInfo = VideoNameParser.getMovieInfo( p );
					if ( movieInfo != null ) {
						try {
							MovieManager.getInstance().setMovieInfo( movie, movieInfo );
						} catch (MovieDbException e) {
							ErrorManager.getInstance().reportThrowable( e );
						}
					}
				}

				boolean mustRefresh = false;
				if ( !DownloadableManager.hasImage( movie )) {
					mustRefresh = true;
				}
				if ( movie.getImdbID() == null && movie.getMovieDbId() > 0 ) {
					mustRefresh = true;
				}
				
				if (mustRefresh && movie.getMovieDbId() > 0) {
					try {
						MovieInfo movieDb = MovieManager.getInstance().getMovieInfo( movie.getMovieDbId() );
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

		if (subtitlesLanguage != null && !VideoManager.isAlreadySubtitled( movie, subtitlesLanguage)) {
			BackLogProcessor.getInstance().schedule( new FindMovieSubtitleTask( movie, subtitlesLanguage ), false );
		}

		imdbIds.add( movie.getImdbID() );		
	}

	protected Movie handleNewMovie( Path movieFile ) throws IOException, InterruptedException, MovieDbException, ParseException {

		Movie movie = null;
		ParsedMovieInfo movieInfo = VideoNameParser.getMovieInfo( movieFile );
		MovieInfo movieDb = null;
		if (movieInfo != null) {
			movieDb = MovieManager.getInstance().searchByName( movieInfo.getName(), movieInfo.getYear(), null, false);
			if (movieDb != null && movieDb.getImdbID() != null) {
				movie = MovieManager.getInstance().findByImdbId( movieDb.getImdbID() );
			}
		}
		if (movie != null) {
			if (movie.getStatus() != DownloadableStatus.DOWNLOADED) {
				DownloadableManager.getInstance().updateStatus( movie, DownloadableStatus.DOWNLOADED);
			}
		} else {
			// create new movie
			String movieName = movieDb != null ? movieDb.getTitle() : movieFile.getFileName().toString();

			int year = movieInfo != null ? movieInfo.getYear() : -1;
			if (movieDb != null && StringUtils.isNotBlank( movieDb.getReleaseDate()))	{
				LocalDate date = LocalDate.parse( movieDb.getReleaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				year = date.getYear();
			}
			
			long id = downloadableDAO.createDownloadable( Movie.class, movieName, DownloadableStatus.DOWNLOADED, year );
			movie = new Movie(
					id, DownloadableStatus.DOWNLOADED, null, movieName, null, null, null, null, null, null, null, null, -1, null, null, -1, year, false );
			if ( movieInfo != null ) {
				try {
					MovieManager.getInstance().setMovieInfo( movie, movieInfo );
				} catch (MovieDbException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
			}
		}

		if (movie.getRating() <= 0 || movie.getYear() <=0 && movie.getImdbID() != null ) {
			IMDBTitle imdbInfo = IMDBWatchListSuggester.extractIMDBTitle( movie.getImdbID() );
			if (imdbInfo != null) {
				movie.setRating( imdbInfo.getRating() );
				movie.setYear( imdbInfo.getYear() );
			}
		}

		MovieManager.getInstance().save( movie );

		DownloadableManager.getInstance().addAssociatedFiles(movieFile, movie);
		DownloadableManager.getInstance().addFile( movie, movieFile );

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
