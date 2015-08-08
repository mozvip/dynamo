package dynamo.backlog.tasks.movies;

import java.nio.file.Path;

import com.omertron.themoviedbapi.model.MovieDb;

import dynamo.core.model.Task;

public class ImportMovieFileTask extends Task {
	
	private Path movieFilePath;
	private MovieDb movieDb;

	public ImportMovieFileTask(Path movieFilePath, MovieDb movieDb) {
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

	public MovieDb getMovieDb() {
		return movieDb;
	}

	public void setMovieDb(MovieDb movieDb) {
		this.movieDb = movieDb;
	}
	
	@Override
	public String toString() {
		return String.format("Post processing movie %s", movieDb.getTitle());
	}
	
}
