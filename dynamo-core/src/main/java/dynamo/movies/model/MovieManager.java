package dynamo.movies.model;

import java.io.IOException;
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
import com.omertron.themoviedbapi.enumeration.SearchType;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;

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
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadableStatus;
import dynamo.movies.jdbi.MovieDAO;
import dynamo.parsers.ParsedMovieInfo;
import dynamo.suggesters.RefreshMovieSuggestionTask;
import dynamo.suggesters.movies.IMDBTitle;
import dynamo.suggesters.movies.IMDBWatchListSuggester;
import hclient.HTTPClient;

public class MovieManager implements Reconfigurable {
	
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
	
	private MovieManager() {
		try {
			api = new TheMovieDbApi("5a1a77e2eba8984804586122754f969f", new YAMJHttpClient( HTTPClient.getInstance() ));
		} catch (MovieDbException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
	}
	
	public ResultList<MovieInfo> search( String name, int year, Language language ) throws MovieDbException {
		return api.searchMovie( name, 0, language != null ? language.getShortName() : null, true, year > 0 ? year : 0, year > 0 ? year : 0, SearchType.PHRASE );
	}
	
	public MovieInfo getMovieInfo( int movieId, String language) throws MovieDbException {
		return api.getMovieInfo(movieId, language);
	}
	
	public String getImageURL( String imagePath ) throws MovieDbException {
		return api.createImageUrl(imagePath, "w185").toExternalForm();
	}
	
	public MovieInfo getMovieInfo( int movieId ) throws MovieDbException {
		return getMovieInfo( movieId, metaDataLanguage != null ? metaDataLanguage.getShortName() : null );	
	}

	public VideoQuality getDefaultQuality() {
		return defaultQuality;
	}

	public void setDefaultQuality(VideoQuality defaultQuality) {
		this.defaultQuality = defaultQuality;
	}
	
	public Movie associate( long movieId, MovieInfo movieDb) throws MovieDbException, IOException {
		Movie movie = movieDAO.find(movieId);
		associate(movie, movieDb);
		save( movie );
		return movie;
	}

	public void associate( Movie movie, MovieInfo movieDb ) throws MovieDbException, IOException {	
		if (movieDb.getImdbID() == null) {
			movieDb = getMovieInfo(movieDb.getId());
		}

		if (movie.getName().equals(movieDb.getTitle())) {
			DownloadableManager.getInstance().updateName( movie.getId(), movieDb.getTitle());
			movie.setName( movieDb.getTitle() );
		}
		movie.setMovieDbId( movieDb.getId() );
		movie.setImdbID( movieDb.getImdbID() );

		if (!DownloadableManager.hasImage( movie ) && movieDb.getPosterPath() != null) {
			DownloadableManager.downloadImage(movie, getImageURL( movieDb.getPosterPath()), null);
		}
		if ( movie.getYear() <= 0 && StringUtils.isNotBlank( movieDb.getReleaseDate() )) {
			movie.setYear( Integer.parseInt( RegExp.extract( movieDb.getReleaseDate(), "(\\d{4}).*") ) );
			DownloadableManager.getInstance().updateYear( movie.getId(), movie.getYear());
		}
	}

	public Movie createMovieFromMovieDB( MovieInfo movieDb, Language language, WebResource defaultImage, DownloadableStatus status, boolean watched ) throws MovieDbException, IOException {
		Movie movie = movieDAO.findByMovieDbId( movieDb.getId() );
		if (movie == null) {
			if (movieDb.getImdbID() == null) {
				movieDb = api.getMovieInfo( movieDb.getId(), language.getShortName());
			}

			long downloadableId = DownloadableManager.getInstance().createDownloadable( Movie.class, movieDb.getTitle(), status );
			if (movieDb.getPosterPath() != null) {
				DownloadableManager.downloadImage(Movie.class, downloadableId, getImageURL( movieDb.getPosterPath()), null);
			} else if (defaultImage != null) {
				DownloadableManager.downloadImage(Movie.class, downloadableId, defaultImage.getUrl(), defaultImage.getReferer() );
			}

			Language originalLanguage = Language.EN;
			if (movieDb.getSpokenLanguages() != null && movieDb.getSpokenLanguages().size() > 0) {
				com.omertron.themoviedbapi.model.Language l = movieDb.getSpokenLanguages().get(0);
				originalLanguage = Language.getByShortName( l.getCode() );	// FIXME : may not always match
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
			
			movie = new Movie( downloadableId, status, null, movieDb.getTitle(), null, null, false, getDefaultQuality(), getAudioLanguage(), getSubtitlesLanguage(), originalLanguage, null, null, null, movieDb.getId(), movieDb.getImdbID(), null, movieDb.getVoteAverage(), year, watched );
		}
		associate( movie, movieDb );
		save( movie );

		return movie;
	}
	
	public MovieInfo getMovieDb( String name, int year ) throws MovieDbException {
		ResultList<MovieInfo> movieDbResults = search( name, year, MovieManager.getInstance().getMetaDataLanguage() );
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

	public void setMovieInfo(Movie movie, ParsedMovieInfo movieInfo ) throws MovieDbException, IOException {
		movie.setName( movieInfo.getName() );
		MovieInfo movieDb = getMovieDb( movieInfo.getName(), movieInfo.getYear() );
		if (movieDb != null) {
			associate(movie, movieDb);
		}
		movie.setQuality( movieInfo.getQuality() );
		movie.setReleaseGroup( movieInfo.getRelease() );
		movie.setSource( movieInfo.getSource() );
	}
	
	public Movie wantMovie( MovieInfo movieDb, VideoQuality wantedQuality, Language wantedAudioLanguage, Language wantedSubtitlesLanguage ) throws IOException, MovieDbException {

		Movie movie = createMovieFromMovieDB( movieDb, getMetaDataLanguage(), null, DownloadableStatus.WANTED, false );

		movie.setWantedAudioLanguage(wantedAudioLanguage);
		movie.setWantedSubtitlesLanguage(wantedSubtitlesLanguage);
		movie.setWantedQuality(wantedQuality);
		
		DownloadableManager.getInstance().want( movie );
		save( movie );

		return movie;
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
				movie.getQuality(), movie.getRating(), movie.getReleaseGroup(), movie.getSource(), movie.isSubtitled(), movie.getSubtitlesPath(),
				movie.getTraktUrl(), movie.getWantedAudioLanguage(), movie.getWantedSubtitlesLanguage(), movie.getQuality(), movie.isWatched()
		);
		DownloadableManager.getInstance().updateYear( movie.getId(), movie.getYear() );	
	}
	
	public Movie suggestImdbId(String imdbId, WebResource defaultImage, Language language, String suggestionURL ) throws MovieDbException, ParseException, IOException, URISyntaxException {
		Movie movie = createByImdbID(imdbId, defaultImage, language, DownloadableStatus.SUGGESTED, false);
		if (movie != null && suggestionURL != null) {
			DownloadableManager.getInstance().saveSuggestionURL( movie.getId(), suggestionURL );
		}
		return movie;
	}

	public Movie createByImdbID(String imdbId, WebResource defaultImage, Language language, DownloadableStatus status, boolean watched) throws MovieDbException, ParseException, IOException, URISyntaxException {
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
			if ( !imdbTitle.isReleased() ) {
				return null;
			}
			if ( imdbTitle.getRating() > 0 && imdbTitle.getRating() <= getMinimumSuggestionRating() ) {
				return null;
			}
			return createMovieFromMovieDB( api.getMovieInfoImdb(imdbId, language.getShortName()), language, defaultImage, status, watched );
		}
	}
	
	public MovieInfo searchByName( String name, int year, Language language, boolean maybeUnreleased ) throws MovieDbException, ParseException {
		MovieInfo selectedMovie = null;
		ResultList<MovieInfo> movieResults = search( name, year, language);
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
			selectedMovie = getMovieInfo( selectedMovie.getId() );
		}
		return selectedMovie;
	}
	
	public Movie findByImdbId( String imdbId ) {
		return movieDAO.findByImdbId( imdbId );
	}

	public Movie suggestByName( String name, int year, WebResource defaultImage, Language language, boolean maybeUnreleased, String suggestionURL ) throws MovieDbException, IOException, URISyntaxException, ParseException {
		MovieInfo movieDb = searchByName( name, year, language, maybeUnreleased);
		if (movieDb != null && movieDb.getImdbID() != null) {
			Movie movie = findByImdbId( movieDb.getImdbID() );
			if (movie == null) {
				
				if (defaultImage == null) {
					defaultImage = new WebResource(getImageURL( movieDb.getPosterPath()));
				}
				
				movie = suggestImdbId( movieDb.getImdbID(), defaultImage, language, suggestionURL );
			}
			return movie;
		} else {
			ErrorManager.getInstance().reportWarning(String.format("MovieDB match not found for %s (%d)", name, year));
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
