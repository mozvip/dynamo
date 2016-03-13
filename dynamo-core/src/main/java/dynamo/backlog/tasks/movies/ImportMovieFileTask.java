package dynamo.backlog.tasks.movies;

import java.nio.file.Path;

import com.omertron.themoviedbapi.model.movie.MovieInfo;

import dynamo.core.model.Task;

public class ImportMovieFileTask extends Task {
	
	private Path movieFilePath;
	private MovieInfo movieDb;

	public ImportMovieFileTask(Path movieFilePath, MovieInfo movieDb) {
		super();
		this.movieFilePath = movieFilePath;
		this.movieDb = movieDb;
	}

	public Path getMovieFilePath() {
		return movieFilePath;
	}

	public void setMovieFilePath(Path movieFilePath) {
		this.movieFilePath = movieFilePath;
	}

	public MovieInfo getMovieDb() {
		return movieDb;
	}

	public void setMovieDb(MovieInfo movieDb) {
		this.movieDb = movieDb;
	}
	
	@Override
	public String toString() {
		return String.format("Post processing movie %s", movieDb.getTitle());
	}
	
}
