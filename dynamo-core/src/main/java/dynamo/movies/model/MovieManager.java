package dynamo.movies.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebResource;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.movies.FindMovieImageTask;
import dynamo.backlog.tasks.movies.MovieCleanupTask;
import dynamo.backlog.tasks.movies.ScanMovieFolderTask;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.MovieProvider;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadableStatus;
import dynamo.movies.jdbi.MovieDAO;
import dynamo.parsers.ParsedMovieInfo;
import dynamo.suggesters.RefreshMovieSuggestionTask;
import dynamo.suggesters.movies.IMDBTitle;
import dynamo.suggesters.movies.IMDBWatchListSuggester;

public class MovieManager implements Reconfigurable {
	
	@Configurable( defaultValue="_1080p" )
	private VideoQuality defaultQuality;

	@Configurable( defaultValue="EN", defaultLabel="Original Language" )
	private Language metaDataLanguage;

	@Configurable( defaultValue="", defaultLabel="Original Language" )
	private Language audioLanguage;

	@Configurable
	private Language subtitlesLanguage;

	@Configurable( contentsClass=Path.class )
	private List<Path> folders;
	
	@Configurable(contentsClass=MovieProvider.class, ordered=true)
	private List<MovieProvider> movieDownloadProviders;
	
	@Configurable( defaultValue="5000" )
	private int minimumSizeFor1080;

	@Configurable( defaultValue="3000" )
	private int minimumSizeFor720;

	@Configurable( defaultValue="20000" )
	private int maximumSizeFor1080;

	@Configurable( defaultValue="10000" )
	private int maximumSizeFor720;
	
	@Configurable( defaultValue="7" )
	private int minimumSuggestionRating;
	
	@Configurable( contentsClass=String.class )
	private Collection<String> wordsBlackList;	

	private MovieDAO movieDAO = DAOManager.getInstance().getDAO( MovieDAO.class );

	public Language getMetaDataLanguage() {
		return metaDataLanguage;
	}

	public void setMetaDataLanguage(Language metaDataLanguage) {
		this.metaDataLanguage = metaDataLanguage;
	}

	public Language getAudioLanguage() {
		return audioLanguage;
	}

	public void setAudioLanguage(Language audioLanguage) {
		this.audioLanguage = audioLanguage;
	}

	public Language getSubtitlesLanguage() {
		return subtitlesLanguage;
	}

	public void setSubtitlesLanguage(Language subtitlesLanguage) {
		this.subtitlesLanguage = subtitlesLanguage;
	}

	public List<MovieProvider> getMovieDownloadProviders() {
		return movieDownloadProviders;
	}

	public void setMovieDownloadProviders(List<MovieProvider> movieDownloadProviders) {
		this.movieDownloadProviders = movieDownloadProviders;
	}
	
	public int getMinimumSizeFor1080() {
		return minimumSizeFor1080;
	}

	public void setMinimumSizeFor1080(int minimumSizeFor1080) {
		this.minimumSizeFor1080 = minimumSizeFor1080;
	}

	public int getMinimumSizeFor720() {
		return minimumSizeFor720;
	}

	public void setMinimumSizeFor720(int minimumSizeFor720) {
		this.minimumSizeFor720 = minimumSizeFor720;
	}

	public int getMaximumSizeFor1080() {
		return maximumSizeFor1080;
	}

	public void setMaximumSizeFor1080(int maximumSizeFor1080) {
		this.maximumSizeFor1080 = maximumSizeFor1080;
	}

	public Collection<String> getWordsBlackList() {
		return wordsBlackList;
	}

	public void setWordsBlackList(Collection<String> wordsBlackList) {
		this.wordsBlackList = wordsBlackList;
	}

	public int getMaximumSizeFor720() {
		return maximumSizeFor720;
	}

	public void setMaximumSizeFor720(int maximumSizeFor720) {
		this.maximumSizeFor720 = maximumSizeFor720;
	}

	public int getMinimumSuggestionRating() {
		return minimumSuggestionRating;
	}

	public void setMinimumSuggestionRating(int minimumSuggestionRating) {
		this.minimumSuggestionRating = minimumSuggestionRating;
	}

	static class SingletonHolder {
		static MovieManager instance = new MovieManager();
	}
	
	public static MovieManager getInstance() {
		return SingletonHolder.instance;
	}

	public VideoQuality getDefaultQuality() {
		return defaultQuality;
	}

	public void setDefaultQuality(VideoQuality defaultQuality) {
		this.defaultQuality = defaultQuality;
	}
	
	public Movie associate( long movieId, MovieInfo movieDb) throws MovieDbException, IOException, InterruptedException {
		Movie movie = movieDAO.find(movieId);
		associate(movie, movieDb);
		save( movie );
		return movie;
	}

	public void associate( Movie movie, MovieInfo movieDb ) throws MovieDbException, IOException, InterruptedException {	
		if (movieDb.getImdbID() == null) {
			movieDb = TheMovieDB.getInstance().getMovieInfo(movieDb.getId(), metaDataLanguage);
		}

		if (!movie.getName().equals(movieDb.getTitle())) {
			DownloadableManager.getInstance().updateName( movie.getId(), movieDb.getTitle());
			movie.setName( movieDb.getTitle() );
		}
		movie.setMovieDbId( movieDb.getId() );
		movie.setImdbID( movieDb.getImdbID() );

		if ( StringUtils.isNotBlank( movieDb.getReleaseDate() )) {
			int year = Integer.parseInt( RegExp.extract( movieDb.getReleaseDate(), "(\\d{4}).*") );
			if (movie.getYear() != year) {
				movie.setYear( year );
				DownloadableManager.getInstance().updateYear( movie.getId(), movie.getYear());
			}
		}
		if (movie.getStatus() != DownloadableStatus.IGNORED && !DownloadableManager.hasImage( movie )) {
			BackLogProcessor.getInstance().schedule( new FindMovieImageTask( movie ), false);
		}
	}

	public Movie createMovieFromMovieDB( MovieInfo movieDb, WebResource defaultImage, DownloadableStatus status, float imdbRating, boolean watched ) throws MovieDbException, IOException, InterruptedException {
		Movie movie = movieDAO.findByMovieDbId( movieDb.getId() );
		if (movie == null) {
			if (movieDb.getImdbID() == null) {
				movieDb = TheMovieDB.getInstance().getMovieInfo( movieDb.getId(), metaDataLanguage );
			}

			int year = -1;
			if (StringUtils.isNotBlank( movieDb.getReleaseDate() )) {
				LocalDate date = LocalDate.parse( movieDb.getReleaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				year = date.getYear();
			}

			long downloadableId = DownloadableManager.getInstance().createDownloadable( Movie.class, movieDb.getTitle(), year, status );
			if (defaultImage != null) {
				DownloadableManager.downloadImage(Movie.class, downloadableId, defaultImage.getUrl(), defaultImage.getReferer() );
			}

			Language originalLanguage = Language.EN;
			if (movieDb.getSpokenLanguages() != null && movieDb.getSpokenLanguages().size() > 0) {
				com.omertron.themoviedbapi.model.Language l = movieDb.getSpokenLanguages().get(0);
				originalLanguage = Language.getByShortName( l.getCode() );	// FIXME : may not always match
			}

			float rating = imdbRating <= 0 ? movieDb.getVoteAverage() : imdbRating;
			
			movie = new Movie( downloadableId, status, null, movieDb.getTitle(), null, getDefaultQuality(), getAudioLanguage(), getSubtitlesLanguage(), originalLanguage, null, null, null, movieDb.getId(), movieDb.getImdbID(), null, rating, year, watched );
		}
		associate( movie, movieDb );
		save( movie );

		return movie;
	}
	
	public MovieInfo getMovieDb( String name, int year ) throws MovieDbException, InterruptedException {
		ResultList<MovieInfo> movieDbResults = TheMovieDB.getInstance().search( name, year, MovieManager.getInstance().getMetaDataLanguage() );
		if (movieDbResults.getTotalResults() == 1) {
			return movieDbResults.getResults().get( 0 );
		} else if (movieDbResults.getTotalResults() > 0) {
			for (MovieInfo movieDb : movieDbResults.getResults()) {
				if (nameEquals( name, movieDb.getTitle())) {
					return movieDb;
				}
			}
		}
		
		return null;
	}

	public void setMovieInfo(Movie movie, ParsedMovieInfo movieInfo, MovieInfo movieDb ) throws MovieDbException, IOException, InterruptedException {
		movie.setName( movieInfo.getName() );
		if (movieDb == null) {
			movieDb = getMovieDb( movieInfo.getName(), movieInfo.getYear() );
		}
		if (movieDb != null) {
			associate(movie, movieDb);
		}
		movie.setQuality( movieInfo.getQuality() );
		movie.setReleaseGroup( movieInfo.getRelease() );
		movie.setSource( movieInfo.getSource() );
	}

	public boolean isEnabled() {
		return folders != null && folders.size() > 0;
	}
	
	public List<Path> getFolders() {
		return folders;
	}

	public void setFolders(List<Path> folders) {
		this.folders = folders;
	}
	
	public List<Movie> getAllMovies() {
		return movieDAO.find();
	}
	
	private Set<String> watchedImdbIds = new HashSet<String>();
	
	public Set<String> getWatchedImdbIds() {
		return watchedImdbIds;
	}

	@Override
	public void reconfigure() {
		if ( isEnabled() ) {
			for (Path path : getFolders()) {
				BackLogProcessor.getInstance().schedule( new ScanMovieFolderTask( path ), false );
			}
		} else {
			BackLogProcessor.getInstance().unschedule( RefreshMovieSuggestionTask.class );
			BackLogProcessor.getInstance().unschedule( ScanMovieFolderTask.class );
			BackLogProcessor.getInstance().unschedule( MovieCleanupTask.class );
		}
	}

	public List<Movie> getSuggestions() {
		return movieDAO.findSuggested();
	}

	public int getMinimumSizeForMovie(VideoQuality videoQuality) {
		int minSize = 0;
		
		if ( videoQuality.equals( VideoQuality._1080p )) {
			return getMinimumSizeFor1080();
		}
		
		if ( videoQuality.equals( VideoQuality._720p )) {
			return getMinimumSizeFor720();
		}

		return minSize;
	}

	public int getMaximumSizeForMovie(VideoQuality videoQuality) {
		int maxSize = 0;
		
		if ( videoQuality.equals( VideoQuality._1080p )) {
			return getMaximumSizeFor1080();
		}
		
		if ( videoQuality.equals( VideoQuality._720p )) {
			return getMaximumSizeFor720();
		}

		return maxSize;
	}

	public List<Movie> getMovieCollection() {
		return movieDAO.findDownloaded();
	}
	
	public List<Movie> getWantedMovies() {
		return movieDAO.findWanted();
	}
	
	public List<Movie> findByStatus( DownloadableStatus status ) {
		return movieDAO.findByStatus( status );
	}

	public Movie deleteMovie( long movieId ) {
		Movie movie = movieDAO.find( movieId );
		BackLogProcessor.getInstance().schedule( new DeleteDownloadableTask( movie ));
		return movie;
	}

	public void save(Movie movie) {
		movieDAO.save(
				movie.getId(), movie.getImdbID(), movie.getMovieDbId(), movie.getOriginalLanguage(),
				movie.getQuality(), movie.getRating(), movie.getReleaseGroup(), movie.getSource(),
				movie.getTraktUrl(), movie.getWantedAudioLanguage(), movie.getWantedSubtitlesLanguage(), movie.getQuality(), movie.isWatched()
		);
		DownloadableManager.getInstance().updateYear( movie.getId(), movie.getYear() );	
	}
	
	public Movie suggestImdbId(String imdbId, WebResource defaultImage, Language language, String suggestionURL ) throws MovieDbException, ParseException, IOException, URISyntaxException, InterruptedException {
		Movie movie = createByImdbID(imdbId, defaultImage, language, DownloadableStatus.SUGGESTED, false);
		if (movie != null && suggestionURL != null) {
			DownloadableManager.getInstance().saveSuggestionURL( movie.getId(), suggestionURL );
		}
		return movie;
	}

	public Movie createByImdbID(String imdbId, WebResource defaultImage, Language language, DownloadableStatus status, boolean watched) throws MovieDbException, ParseException, IOException, URISyntaxException, InterruptedException {
		IMDBTitle imdbTitle = IMDBWatchListSuggester.extractIMDBTitle(imdbId);
		
		Movie movie = movieDAO.findByImdbId( imdbId );
		if (movie != null) {
			
			if (movie.getStatus() != status && movie.getStatus() != DownloadableStatus.DOWNLOADED && movie.getStatus() != DownloadableStatus.WANTED && movie.getStatus() != DownloadableStatus.SNATCHED) {
				DownloadableManager.getInstance().updateStatus( movie, status);
			}

			return movie;

		} else {
		
			if (imdbTitle == null || imdbTitle.isTvSeries() ) {
				return null;
			}
			if (imdbTitle.getGenres() != null && imdbTitle.getGenres().contains("Short")) {
				return null;
			}
			if (imdbTitle.getGenres() != null && imdbTitle.getGenres().contains("Documentary")) {
				return null;
			}
			if ( !imdbTitle.isReleased() ) {
				return null;
			}

			try {
				MovieInfo movieInfoImdb = TheMovieDB.getInstance().getMovieInfoImdb(imdbId, language );
				return createMovieFromMovieDB( movieInfoImdb, defaultImage, status, imdbTitle.getRating(), watched );
			} catch (MovieDbException e) {
				if (e.getResponseCode() != 404) {
					throw e;
				}
			}
			
			return null;
		}
	}
	
	public MovieInfo searchByName( String name, int year, Language language, boolean maybeUnreleased ) throws MovieDbException, ParseException, InterruptedException {
		MovieInfo selectedMovie = null;
		ResultList<MovieInfo> movieResults = TheMovieDB.getInstance().search( name, year, language);
		if (movieResults.getTotalResults() > 0) {
			Date now = new Date();
			for (MovieInfo movieDb : movieResults.getResults()) {
				if ( !nameEquals( movieDb.getTitle(), name ) ) {
					continue;
				}
				if (maybeUnreleased && StringUtils.isNotBlank(movieDb.getReleaseDate())) {
					Date releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(movieDb.getReleaseDate());
					if (releaseDate.after( now )) {
						continue;
					}
				}
				selectedMovie = movieDb;
			}
			if (selectedMovie == null) {
				return null;
			}
			selectedMovie = TheMovieDB.getInstance().getMovieInfo( selectedMovie.getId(), metaDataLanguage );
		}
		return selectedMovie;
	}
	
	public Movie findByImdbId( String imdbId ) {
		return movieDAO.findByImdbId( imdbId );
	}
	
	public static String[] movieNameCleanups = new String[] {
			"(.*)\\s+3d",
			"(.*)\\s+IMAX",
			"(.*)\\s+\\(version longue\\)",
			"(.*)\\s+\\(unrated\\)",
			"(.*)\\s+\\(extended edition\\)",
			"(.*)\\s+\\(ultimate edition\\)",
			"(.*)\\s+bluray",
			"(.*)\\s+multi vff",
			"(.*)\\s+1080p",
			"(.*)\\s+1080",
			"(.*)\\s+720p",
			"(.*)\\s+720",
			"(.*)\\s+vff",
			"(.*)\\s+hdrip",
			"(.*)\\s+vostfr",
			"(.*)\\s+truefrench",
			"(.*)\\s+\\([^(]*\\s+cut\\)"
	};

	public Movie suggestByName( String name, int year, WebResource defaultImage, Language language, boolean maybeUnreleased, String suggestionURL ) {

		name = RegExp.clean(name, movieNameCleanups);
		try {
			MovieInfo movieDb = searchByName( name, year, language, maybeUnreleased);
			if (movieDb != null && movieDb.getImdbID() != null) {
				Movie movie = findByImdbId( movieDb.getImdbID() );
				if (movie == null) {
					
					if (defaultImage == null) {
						defaultImage = new WebResource(TheMovieDB.getInstance().getImageURL( movieDb.getPosterPath()));
					}
					
					movie = suggestImdbId( movieDb.getImdbID(), defaultImage, language, suggestionURL );
				}
				return movie;
			} else {
				ErrorManager.getInstance().reportWarning(String.format("MovieDB match not found for %s (%d)", name, year));
			}
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
		return null;
	}


	private boolean nameEquals(String title, String name) {
		
		title = title.replaceAll("[\\W]", "").toLowerCase();
		name = name.replaceAll("[\\W]", "").toLowerCase();
		
		return title.equals( name );
	}

	public void setWatched(String imdbId) {
		watchedImdbIds.add( imdbId );
	}

}
