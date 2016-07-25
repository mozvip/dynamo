package dynamo.backlog.tasks.movies;

import dynamo.core.model.Task;
import dynamo.movies.model.Movie;

public class RenameMovieFileTask extends Task {
	
	private Movie movie;

	public RenameMovieFileTask(Movie movie) {
		this.movie = movie;
	}
	
	public Movie getMovie() {
		return movie;
	}
	
	public void setMovie(Movie movie) {
		this.movie = movie;
	}
	
	@Override
	public String toString() {
		return String.format("Renaming files for movie %s", movie.toString() );
	}

}
