package dynamo.backlog.tasks.movies;

import java.io.IOException;
import java.nio.file.Path;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

import dynamo.backlog.tasks.core.FindDownloadableImageExecutor;
import dynamo.core.manager.ErrorManager;
import dynamo.manager.DownloadableManager;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.suggesters.movies.IMDBTitle;
import dynamo.suggesters.movies.IMDBWatchListSuggester;

public class FindMovieImageExecutor extends FindDownloadableImageExecutor<Movie> {

	public FindMovieImageExecutor(FindMovieImageTask task) {
		super(task);
	}

	@Override
	public boolean downloadImageTo(Path localImage) {

		Movie movie = task.getDownloadable();

		// try from MovieDB first
		if (movie.getMovieDbId() > 0) {
			try {
				MovieInfo movieDb = MovieManager.getInstance().getMovieInfo( movie.getMovieDbId() );
				if ( movieDb.getPosterPath() != null ) {
					DownloadableManager.downloadImage(movie, MovieManager.getInstance().getImageURL( movieDb.getPosterPath()), null);
					return true;
				}
			} catch (MovieDbException | IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}

		// and then from IMDB
		if (movie.getImdbID() != null) {
			try {
				IMDBTitle imdbData = IMDBWatchListSuggester.extractIMDBTitle( movie.getImdbID() );
				if (imdbData.getImage() != null) {
					DownloadableManager.downloadImage(movie, imdbData.getImage().getUrl(), imdbData.getImage().getReferer());
				}
				return true;
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}

		return false;
	}

}
