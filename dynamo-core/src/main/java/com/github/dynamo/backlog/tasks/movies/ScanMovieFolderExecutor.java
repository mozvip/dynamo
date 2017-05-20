package com.github.dynamo.backlog.tasks.movies;

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

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.VideoFileFilter;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.ReportProgress;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.FolderManager;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.movies.model.Movie;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.movies.model.TheMovieDB;
import com.github.dynamo.parsers.ParsedMovieInfo;
import com.github.dynamo.parsers.VideoNameParser;
import com.github.dynamo.subtitles.FindMovieSubtitleTask;
import com.github.dynamo.suggesters.movies.IMDBTitle;
import com.github.dynamo.suggesters.movies.IMDBWatchListSuggester;
import com.github.dynamo.video.VideoManager;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

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
				if (path.getFileName().toString().contains(".sample.")) {
					continue;
				}
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

				if ( movie.getImdbID() == null && movie.getMovieDbId() > 0 ) {
					try {
						MovieInfo movieDb = TheMovieDB.getInstance().getMovieInfo( movie.getMovieDbId(), null );
						MovieManager.getInstance().associate(movie, movieDb);
					} catch (MovieDbException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}							
				} else {
					if ( !DownloadableManager.hasImage( movie )) {
						if (movie.getImdbID() != null || movie.getMovieDbId() > 0) {
							BackLogProcessor.getInstance().schedule( new FindMovieImageTask( movie ), false);
						}
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

		if (movie == null) {
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
