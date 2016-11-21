package dynamo.backlog.tasks.movies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.core.Language;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.manager.FolderManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.subtitles.FindMovieSubtitleTask;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.movies.model.TheMovieDB;
import dynamo.parsers.ParsedMovieInfo;
import dynamo.parsers.VideoNameParser;
import dynamo.suggesters.movies.IMDBTitle;
import dynamo.suggesters.movies.IMDBWatchListSuggester;
import dynamo.video.VideoManager;

public class ScanMovieFolderExecutor extends TaskExecutor<ScanMovieFolderTask> implements ReportProgress {
	
	protected int totalItems;
	protected int itemsDone;
	
	@Override
	public int getTotalItems() {
		return totalItems;
	}

	@Override
	public int getItemsDone() {
		return itemsDone;
	}	
	
	private Set<String> imdbIds = new HashSet<String>();

	private DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );

	private Language subtitlesLanguage;

	public ScanMovieFolderExecutor( ScanMovieFolderTask item ) {
		super(item);
		subtitlesLanguage = MovieManager.getInstance().getSubtitlesLanguage();
	}
	
	public List<Path> getAssociatedFiles( Path file, List<Path> fromList ) {
		List<Path> matches = new ArrayList<>();
		String fileName = file.getFileName().toString();
		final String filePrefix = fileName.substring(0,  fileName.lastIndexOf('.'));
		for (Path path : fromList) {
			if (path.getFileName().toString().startsWith( filePrefix )) {
				matches.add( path );
			}
		}
		return matches;
	}
	
	@Override
	public void execute() throws Exception {
		List<Path> contents = FolderManager.getInstance().getContents(task.getFolder(), null, true);
		totalItems = contents.size();		
		for (Path path : contents) {
			if (VideoFileFilter.getInstance().accept( path )) {
				addMovie( getAssociatedFiles( path, contents ) );
			}
			itemsDone ++;
		}
	}

	private void addMovie(List<Path> fileGroup) throws IOException, InterruptedException {
		
		Path movieFile = fileGroup.stream().filter( path -> VideoFileFilter.getInstance().accept( path ) ).max( (p1, p2) -> {
			try {
				return Long.compare(Files.size( p1 ), Files.size(p2));
			} catch (IOException e) {
				return 0;
			}
		}).get();
		
		DownloadableFile dFile = DownloadableManager.getInstance().getFile( movieFile );
		
		Movie movie = null;

		if (dFile != null) {
			
			// movie is already downloaded
			Downloadable downloadable = DownloadableFactory.getInstance().createInstance( dFile.getDownloadableId() );
			if (!(downloadable instanceof Movie)) {
				// ??
				return;
			}
			
			movie = (Movie) downloadable;

			if ( movie.getImdbID() != null && imdbIds.contains( movie.getImdbID()) ) {
				// duplicate
				DownloadableManager.getInstance().delete( movie.getId() );
			} else {
				VideoManager.getInstance().getMetaData( movie, movieFile );

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
					ParsedMovieInfo movieInfo = VideoNameParser.getMovieInfo( movieFile );
					if ( movieInfo != null ) {
						try {
							MovieManager.getInstance().setMovieInfo( movie, movieInfo, null );
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
						MovieInfo movieDb = TheMovieDB.getInstance().getMovieInfo( movie.getMovieDbId(), null );
						MovieManager.getInstance().associate(movie, movieDb);
					} catch (MovieDbException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}							
				}

				MovieManager.getInstance().save( movie );
			}

		} else {
			try {
				movie = handleNewMovie(movieFile, fileGroup );
			} catch (MovieDbException | ParseException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}

		if (subtitlesLanguage != null && !VideoManager.isAlreadySubtitled( movie, subtitlesLanguage)) {
			BackLogProcessor.getInstance().schedule( new FindMovieSubtitleTask( movie, subtitlesLanguage ), false );
		}

		imdbIds.add( movie.getImdbID() );		
	}

	protected Movie handleNewMovie( Path movieFile, List<Path> fileGroup ) throws IOException, InterruptedException, MovieDbException, ParseException {

		Movie movie = null;
		ParsedMovieInfo parsedMovieInfo = VideoNameParser.getMovieInfo( movieFile );
		MovieInfo movieDb = null;
		
		String movieName = movieFile.getFileName().toString();
		
		if (parsedMovieInfo != null) {
			
			movieName = parsedMovieInfo.getName();
			
			movieDb = MovieManager.getInstance().searchByName( movieName, parsedMovieInfo.getYear(), null, false);
			if (movieDb == null && parsedMovieInfo.getYear() != -1) {
				movieDb = MovieManager.getInstance().searchByName( parsedMovieInfo.getName(), -1, null, false);
			}
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
			movieName = movieDb != null ? movieDb.getTitle() : movieName;

			int year = parsedMovieInfo != null ? parsedMovieInfo.getYear() : -1;
			if (movieDb != null && StringUtils.isNotBlank( movieDb.getReleaseDate()))	{
				LocalDate date = LocalDate.parse( movieDb.getReleaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				year = date.getYear();
			}
			
			long id = downloadableDAO.createDownloadable( Movie.class, movieName, DownloadableStatus.DOWNLOADED, year );
			movie = new Movie(
					id, DownloadableStatus.DOWNLOADED, null, movieName, null, null, null, null, null, null, null, null, -1, null, null, -1, year, false );
			if ( parsedMovieInfo != null ) {
				try {
					MovieManager.getInstance().setMovieInfo( movie, parsedMovieInfo, movieDb );
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

		for (Path path : fileGroup) {
			DownloadableManager.getInstance().addFile(movie, path);	
		}
		VideoManager.getInstance().getMetaData( movie, movieFile );
		return movie;
	}

}
