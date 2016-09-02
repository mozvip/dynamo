package dynamo.backlog.tasks.movies;

import java.nio.file.Path;

import com.omertron.themoviedbapi.model.movie.MovieInfo;

import dynamo.backlog.tasks.files.FileUtils;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.FolderManager;
import dynamo.model.DownloadableStatus;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.suggesters.movies.IMDBTitle;
import dynamo.suggesters.movies.IMDBWatchListSuggester;

public class ImportMovieFileTaskExecutor extends TaskExecutor< ImportMovieFileTask > {
	
	MovieInfo movieDb;
	Path path;

	public ImportMovieFileTaskExecutor(ImportMovieFileTask task) {
		super(task);
		this.movieDb = task.getMovieDb();
		this.path = task.getMovieFilePath();
	}

	@Override
	public void execute() throws Exception {
		
		IMDBTitle imdbTitle = IMDBWatchListSuggester.extractIMDBTitle( movieDb.getImdbID() );
		float imdbRating = imdbTitle != null ? imdbTitle.getRating() : 0.0f;
		
		Movie movie = MovieManager.getInstance().createMovieFromMovieDB(
				movieDb, MovieManager.getInstance().getMetaDataLanguage(), null, DownloadableStatus.DOWNLOADED, imdbRating, false
		);
		// move main file
		Path destinationFolder = FileUtils.getFolderWithMostUsableSpace( MovieManager.getInstance().getFolders() );
		if (destinationFolder != null) {
			FolderManager.moveFile( path, destinationFolder.resolve( path.getFileName() ), movie);
			FolderManager.moveAssociatedFiles( path, destinationFolder, movie );
		}
	}

}
