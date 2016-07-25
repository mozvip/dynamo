package dynamo.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

import dynamo.backlog.tasks.movies.RenameMovieFileTask;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.manager.DownloadableManager;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.video.VideoManager;

@ManagedBean
@ViewScoped
public class Movies extends DynamoManagedBean {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Language searchLanguage = MovieManager.getInstance().getMetaDataLanguage();

	private VideoQuality newMovieQuality = MovieManager.getInstance().getDefaultQuality();
	private Language newMovieAudioLanguage = MovieManager.getInstance().getAudioLanguage();
	private Language newMovieSubtitlesLanguage = MovieManager.getInstance().getSubtitlesLanguage();
	private boolean renameFile;
	
	private List<MovieInfo> searchResults;
	private long searchMovieId;
	private String searchMovie;
	
	private DownloadablePager<Movie> collectionContents;
	private DownloadablePager<Movie> suggestionContents;
	private DownloadablePager<Movie> wantedContents;
	
	public DownloadablePager<Movie> getCollectionContents() {
		if (collectionContents == null) {
			collectionContents = new DownloadablePager<>( MovieManager.getInstance().getMovieCollection() );
		}
		return collectionContents;
	}
	
	public DownloadablePager<Movie> getSuggestionContents() {
		if (suggestionContents == null) {
			suggestionContents = new DownloadablePager<>( MovieManager.getInstance().getSuggestions() );
		}
		return suggestionContents;
	}

	public DownloadablePager<Movie> getWantedContents() {
		if (wantedContents == null) {
			wantedContents = new DownloadablePager<>( MovieManager.getInstance().getWantedMovies() );
		}
		return wantedContents;
	}

	public boolean isSeen( Movie movie ) {
		return MovieManager.getInstance().getWatchedImdbIds().contains( movie.getImdbID() );
	}

	public List<SelectItem> getAllFolders() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		List<Path> movieFolders = MovieManager.getInstance().getFolders();
		for (Path folder : movieFolders) {
			items.add(new SelectItem( folder ));
		}
		return items;
	}
	
	public List<SelectItem> getAllQualities() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (VideoQuality quality : VideoQuality.values()) {
			items.add(new SelectItem( quality, quality.getLabel() ));
		}
		return items;
	}

	public String getSearchMovie() {
		return searchMovie;
	}

	public void setSearchMovie(String searchMovie) {
		this.searchMovie = searchMovie;
	}
	
	public List<MovieInfo> getSearchResults() {
		return searchResults;
	}
	
	public void setSearchResults(List<MovieInfo> searchResults) {
		this.searchResults = searchResults;
	}

	public Language getSearchLanguage() {
		return searchLanguage;
	}
	
	public void setSearchLanguage(Language searchLanguage) {
		this.searchLanguage = searchLanguage;
	}

	public long getSearchMovieId() {
		return searchMovieId;
	}

	public void setSearchMovieId(long searchMovieId) {
		this.searchMovieId = searchMovieId;
	}

	public void search() throws MovieDbException {
		searchResults = MovieManager.getInstance().search( searchMovie, 0, searchLanguage ).getResults();
	}

	public void selectMovie( MovieInfo movie ) throws IOException, MovieDbException {
		searchResults = null;
		MovieManager.getInstance().wantMovie( movie, newMovieQuality, newMovieAudioLanguage, newMovieSubtitlesLanguage );
		addMessage("Operation queued successfully");
	}
	
	public void delete() {
		int id = getIntegerParameter("id");
		MovieManager.getInstance().deleteMovie( id );
		if (wantedContents != null) {
			wantedContents.remove(id);
		}
		if (collectionContents != null) {
			collectionContents.remove( id );
		}
	}

	public void select( MovieInfo selectedMovie ) throws MovieDbException, IOException {
		MovieManager.getInstance().associate( searchMovieId, selectedMovie);
		if (isRenameFile()) {
			for (Movie movie : collectionContents.objects) {
				if (movie.getId() == searchMovieId) {
					queue( new RenameMovieFileTask(movie), false );
					break;
				}
			}
		}
	}

	public VideoQuality getNewMovieQuality() {
		return newMovieQuality;
	}

	public void setNewMovieQuality(VideoQuality newMovieQuality) {
		this.newMovieQuality = newMovieQuality;
	}

	public Language getNewMovieAudioLanguage() {
		return newMovieAudioLanguage;
	}

	public void setNewMovieAudioLanguage(Language newMovieAudioLanguage) {
		this.newMovieAudioLanguage = newMovieAudioLanguage;
	}

	public Language getNewMovieSubtitlesLanguage() {
		return newMovieSubtitlesLanguage;
	}

	public void setNewMovieSubtitlesLanguage(Language newMovieSubtitlesLanguage) {
		this.newMovieSubtitlesLanguage = newMovieSubtitlesLanguage;
	}

	public boolean isRenameFile() {
		return renameFile;
	}
	
	public void setRenameFile(boolean renameFile) {
		this.renameFile = renameFile;
	}
	
	public void play( Movie movie ) throws IOException {
		Optional<Path> moviePath = VideoManager.getInstance().getMainVideoFile( movie.getId() );
		if (moviePath.isPresent()) {
			Desktop.getDesktop().open( moviePath.get().toFile() );
		}
	}
	
	public void wantSuggestion() {

		int id = getIntegerParameter("id");
		
		Movie wantedMovie = suggestionContents.remove( id );
		
		wantedMovie.setWantedQuality( MovieManager.getInstance().getDefaultQuality() );
		wantedMovie.setWantedAudioLanguage( MovieManager.getInstance().getAudioLanguage() );
		wantedMovie.setWantedSubtitlesLanguage( MovieManager.getInstance().getSubtitlesLanguage() );
		
		MovieManager.getInstance().save( wantedMovie );

		DownloadableManager.getInstance().want( wantedMovie );
	}

}
