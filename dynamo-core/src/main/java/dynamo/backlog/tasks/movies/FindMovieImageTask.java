package dynamo.backlog.tasks.movies;

import dynamo.backlog.tasks.core.FindDownloadableImageTask;
import dynamo.movies.model.Movie;

public class FindMovieImageTask extends FindDownloadableImageTask<Movie> {

	public FindMovieImageTask(Movie downloadable) {
		super(downloadable);
	}
	
	@Override
	public String toString() {
		return String.format( "Finding image for movie <a href='%s'>%s</a>", getDownloadable().getRelativeLink(), getDownloadable().toString());
	}	

}
