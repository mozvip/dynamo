package dynamo.model.movies;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.MovieDb;
import com.omertron.themoviedbapi.results.TmdbResultsList;

import core.RegExp;
import core.WebResource;
import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.movies.MovieCleanupTask;
import dynamo.backlog.tasks.movies.ScanMovieFolderTask;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.MovieProvider;
import dynamo.httpclient.YAMJHttpClient;
import dynamo.jdbi.MovieDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.DownloadableStatus;
import dynamo.parsers.MovieInfo;
import dynamo.suggesters.RefreshMovieSuggestionTask;
import dynamo.suggesters.movies.IMDBTitle;
import dynamo.suggesters.movies.IMDBWatchListSuggester;
import dynamo.trakt.TraktManager;
import dynamo.trakt.TraktWatchedEntry;
import hclient.HTTPClient;

public class MovieManager implements Reconfigurable {
	
	@Configurable( category="Movies", name="Enable Movies", bold=true )
	private boolean enabled;	
	
	@Configurable( category="Movies", name="Default Quality to Download", defaultValue="_1080p", disabled="#{!MovieManager.enabled}" )
	private VideoQuality defaultQuality;

	@Configurable( category="Movies", name="Default Metadata Language", defaultValue="EN", disabled="#{!MovieManager.enabled}", required="#{MovieManager.enabled}", defaultLabel="Original Language" )
	private Language metaDataLanguage;

	@Configurable( category="Movies", name="Default Audio Language", defaultValue="", disabled="#{!MovieManager.enabled}", defaultLabel="Original Language" )
	private Language audioLanguage;

	@Configurable( category="Movies", name="Default Subtitles Language", disabled="#{!MovieManager.enabled}" )
	private Language subtitlesLanguage;

	@Configurable( category="Movies", name="Movie folders", required="#{MovieManager.enabled}", disabled="#{!MovieManager.enabled}", contentsClass=Path.class )
	private List<Path> folders;
	
	@Configurable(category="Movies", name="Movies Providers", required="#{MovieManager.enabled}", disabled="#{!MovieManager.enabled}", contentsClass=MovieProvider.class, ordered=true)
	private List<MovieProvider> movieDownloadProviders;
	
	@Configurable( category="Movies", name="Minimum size for a 1080p movie (Mb)", required="#{MovieManager.enabled}", disabled="#{!MovieManager.enabled}", defaultValue="5000" )
	private int minimumSizeFor1080;

	@Configurable( category="Movies", name="Minimum size for a 720p movie (Mb)", required="#{MovieManager.enabled}", disabled="#{!MovieManager.enabled}", defaultValue="3000" )
	private int minimumSizeFor720;

	@Configurable( category="Movies", name="Maximum size for a 1080p movie (Mb)", required="#{MovieManager.enabled}", disabled="#{!MovieManager.enabled}", defaultValue="20000" )
	private int maximumSizeFor1080;

	@Configurable( category="Movies", name="Maximum size for a 720p movie (Mb)", required="#{MovieManager.enabled}", disabled="#{!MovieManager.enabled}", defaultValue="10000" )
	private int maximumSizeFor720;
	
	@Configurable( category="Movies", name="Only suggest movies whose rating is >=", required="#{MovieManager.enabled}", disabled="#{!MovieManager.enabled}", defaultValue="7" )
	private int minimumSuggestionRating;
	
	@Configurable(category="Movies", name="Don't suggest watched movies", defaultValue="true", disabled="#{!MovieManager.enabled}")
	private boolean hideWatched;
	
	@Configurable( category="Movies", name="Words Black List", disabled="#{!MovieManager.enabled}", contentsClass=String.class )
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

	public boolean isHideWatched() {
		return hideWatched;
	}

	public void setHideWatched(boolean hideWatched) {
		this.hideWatched = hideWatched;
	}

	private TheMovieDbApi api;
	
	static class SingletonHolder {
		static MovieManager instance = new MovieManager();
	}
	
	public static MovieManager getInstance() {
		return SingletonHolder.instance;
	}
	
	private Set<String> imdbIds = new HashSet<>();
	
	private MovieManager() {
		List<Movie> allMovies = getAllMovies();
		for (Movie movie : allMovies) {
			if (movie.getImdbID() != null) {
				imdbIds.add( movie.getImdbID() );
			}
		}
		if (hideWatched && TraktManager.getInstance().isEnabled()) {
			try {
				List<TraktWatchedEntry> watchedMovies = TraktManager.getInstance().getMoviesWatched();
				for (TraktWatchedEntry watchedEntry : watchedMovies) {
					imdbIds.add( watchedEntry.getMovie().getIds().get("imdb") );
				}
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
		
		try {
			api = new TheMovieDbApi("5a1a77e2eba8984804586122754f969f", new YAMJHttpClient( HTTPClient.getInstance() ));
		} catch (MovieDbException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
		
	}
	
	public TmdbResultsList<MovieDb> search( String name, int year, Language language ) throws MovieDbException {
		return api.searchMovie( name, year > 0 ? year : 0, language != null ? language.getShortName() : null, true, 0 );
	}
	
	public MovieDb getMovieInfo( int movieId, String language) throws MovieDbException {
		return api.getMovieInfo(movieId, language);
	}
	
	public String getImageURL( String imagePath ) throws MovieDbException {
		return api.createImageUrl(imagePath, "w185").toExternalForm();
	}
	
	public MovieDb getMovieInfo( int movieId ) throws MovieDbException {
		return getMovieInfo( movieId, metaDataLanguage != null ? metaDataLanguage.getShortName() : null );	
	}

	public VideoQuality getDefaultQuality() {
		return defaultQuality;
	}

	public void setDefaultQuality(VideoQuality defaultQuality) {
		this.defaultQuality = defaultQuality;
	}
	
	public void associate( long movieId, MovieDb movieDb) throws MovieDbException {
		Movie movie = movieDAO.find(movieId);
		associate(movie, movieDb);
		save( movie );
	}

	public void associate( Movie movie, MovieDb movieDb ) throws MovieDbException {	
		if (movieDb.getImdbID() == null) {
			movieDb = getMovieInfo(movieDb.getId());
		}

		movie.setName( movieDb.getTitle() );
		movie.setMovieDbId( movieDb.getId() );
		movie.setImdbID( movieDb.getImdbID() );

		if (StringUtils.isNotBlank( movieDb.getReleaseDate() )) {
			movie.setYear( Integer.parseInt( RegExp.extract( movieDb.getReleaseDate(), "(\\d{4}).*") ) );
		}

		String coverImage = null;
		if (movieDb.getPosterPath() != null) {
			coverImage = LocalImageCache.getInstance().download( "movies", movie.getImdbID(), getImageURL( movieDb.getPosterPath()), null );		
		} else {
			coverImage = "/downloaded-movie-unknown.jpg";
		}
		if ( movie.getCoverImage() == null || !movie.getCoverImage().equals( coverImage )) {
			DownloadableManager.getInstance().updateCoverImage( movie.getId(), coverImage);
		}
	}

	public Movie createMovieFromMovieDB( MovieDb movieDb, Language language, WebResource defaultImage, DownloadableStatus status ) throws MovieDbException, MalformedURLException {
		Movie movie = movieDAO.findByMovieDbId( movieDb.getId() );
		if (movie == null) {
			if (movieDb.getImdbID() == null) {
				movieDb = api.getMovieInfo( movieDb.getId(), language.getShortName());
			}

			String coverImage = null;
			if (movieDb.getPosterPath() != null) {
				coverImage = LocalImageCache.getInstance().download( "movies", movieDb.getImdbID(), getImageURL( movieDb.getPosterPath()), null );	
			} else {
				coverImage = LocalImageCache.getInstance().download( "movies", movieDb.getImdbID(), defaultImage.getUrl(), defaultImage.getReferer() );
			}
			long downloadableId = DownloadableManager.getInstance().createDownloadable( Movie.class, null, coverImage, status );

			Language originalLanguage = Language.EN;
			if (movieDb.getSpokenLanguages() != null && movieDb.getSpokenLanguages().size() > 0) {
				com.omertron.themoviedbapi.model.Language l = movieDb.getSpokenLanguages().get(0);
				originalLanguage = Language.getByShortName( l.getIsoCode() );	// FIXME : may not always match
			}

			int year = -1;
			if (StringUtils.isNotBlank( movieDb.getReleaseDate() )) {
				Date releaseDate;
				try {
					releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(movieDb.getReleaseDate());
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(releaseDate);
					year = calendar.get(Calendar.YEAR);
				} catch (ParseException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
			}

			movie = new Movie( downloadableId, status, null, coverImage, null, movieDb.getTitle(), null, false, getDefaultQuality(), getAudioLanguage(), getSubtitlesLanguage(), originalLanguage, null, null, null, movieDb.getId(), movieDb.getImdbID(), null, movieDb.getVoteAverage(), year, false );
		}
		associate( movie, movieDb );
		save( movie );

		return movie;
	}
	
	public MovieDb getMovieDb( String name, int year ) throws MovieDbException {
		TmdbResultsList<MovieDb> movieDbResults = search( name, year, MovieManager.getInstance().getMetaDataLanguage() );
		if (movieDbResults.getTotalResults() == 1) {
			return movieDbResults.getResults().get( 0 );
		} else if (movieDbResults.getTotalResults() > 0) {
			for (MovieDb movieDb : movieDbResults.getResults()) {
				if (nameEquals( name, movieDb.getTitle())) {
					return movieDb;
				}
			}
		}
		
		return null;
	}

	public void setMovieInfo(Movie movie, MovieInfo movieInfo ) throws MovieDbException {
		movie.setName( movieInfo.getName() );
		MovieDb movieDb = getMovieDb( movieInfo.getName(), movieInfo.getYear() );
		if (movieDb != null) {
			associate(movie, movieDb);
		}
		movie.setQuality( movieInfo.getQuality() );
		movie.setReleaseGroup( movieInfo.getRelease() );
		movie.setSource( movieInfo.getSource() );
	}
	
	public Movie wantMovie( MovieDb movieDb, VideoQuality wantedQuality, Language wantedAudioLanguage, Language wantedSubtitlesLanguage ) throws IOException, MovieDbException {

		Movie movie = createMovieFromMovieDB( movieDb, getMetaDataLanguage(), null, DownloadableStatus.WANTED );

		movie.setWantedAudioLanguage(wantedAudioLanguage);
		movie.setWantedSubtitlesLanguage(wantedSubtitlesLanguage);
		movie.setWantedQuality(wantedQuality);
		
		DownloadableManager.getInstance().want( movie );
		save( movie );

		return movie;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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
		if (enabled) {
			if (getFolders() != null) {
				for (Path path : getFolders()) {
					if (Files.isReadable( path )) {
						BackLogProcessor.getInstance().schedule( new ScanMovieFolderTask( path ) );
					} else {
						ErrorManager.getInstance().reportWarning(String.format("Movie folder %s is not readable", path.toAbsolutePath().toString()));
					}
				}
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
	

	public Movie deleteMovie( long movieId ) {
		Movie movie = movieDAO.find( movieId );
		BackLogProcessor.getInstance().schedule( new DeleteDownloadableTask( movie ));
		return movie;
	}

	public void save(Movie movie) {
		save(movie.getId(), movie);
	}

	public void save(long downloadableId, Movie movie) {
		movieDAO.save(
				downloadableId, movie.getImdbID(), movie.getMovieDbId(), movie.getName(), movie.getYear(), movie.getOriginalLanguage(),
				movie.getQuality(), movie.getRating(), movie.getReleaseGroup(), movie.getSource(), movie.isSubtitled(), movie.getSubtitlesPath(),
				movie.getTraktUrl(), movie.getWantedAudioLanguage(), movie.getWantedSubtitlesLanguage(), movie.getQuality(), movie.isWatched()
		);
	}

	public Movie suggestByImdbID(String imdbId, WebResource defaultImage, Language language) throws MovieDbException, ParseException, IOException, URISyntaxException {
		if (imdbIds.contains( imdbId )) {
			return null;
		}
		IMDBTitle imdbTitle = IMDBWatchListSuggester.extractIMDBTitle(imdbId);
		if (imdbTitle == null || imdbTitle.isTvSeries() ) {
			return null;
		}
		if (imdbTitle.getGenres() != null && imdbTitle.getGenres().contains("Short")) {
			return null;
		}
		if ( !imdbTitle.isReleased() ) {
			return null;
		}
		if ( imdbTitle.getRating() > 0 && imdbTitle.getRating() <= getMinimumSuggestionRating() ) {
			return null;
		}
		synchronized( this ) {
			imdbIds.add( imdbId );
		}
		return createMovieFromMovieDB( api.getMovieInfoImdb(imdbId, language.getShortName()), language, defaultImage, DownloadableStatus.SUGGESTED );
	}

	public Movie suggestByName( String name, int year, WebResource defaultImage, Language language ) throws MovieDbException, IOException, URISyntaxException, ParseException {
		TmdbResultsList<MovieDb> movieResults = MovieManager.getInstance().search( name, year, language);
		if (movieResults.getTotalResults() > 0) {

			Date now = new Date();
			MovieDb selectedMovie = null;
			Date currentDate = null;

			for (MovieDb movieDb : movieResults.getResults()) {
				if ( !nameEquals( movieDb.getTitle(), name ) ) {
					continue;
				}
				if (StringUtils.isNotBlank(movieDb.getReleaseDate())) {
					Date releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(movieDb.getReleaseDate());
					if (releaseDate.after( now )) {
						continue;
					}
					if ( currentDate == null || releaseDate.after(currentDate)) {
						selectedMovie = movieDb;
						currentDate = releaseDate;
					}
				}
			}
			if (selectedMovie == null) {
				return null;
			}
			selectedMovie = getMovieInfo( selectedMovie.getId() );
			if (selectedMovie.getImdbID() != null) {
				return suggestByImdbID( selectedMovie.getImdbID(), defaultImage, language );
			} else {
				ErrorManager.getInstance().reportWarning(String.format("IMDB match not found for %s (%d)", name, year));
			}
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
		if (hideWatched) {
			// if movie is currently suggested, delete the suggestion
			movieDAO.deleteIfSuggested( imdbId );
		}
	}


}
