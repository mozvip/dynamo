package dynamo.backlog.tasks.movies;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.MovieDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;

public class MovieCleanupExecutor extends TaskExecutor<MovieCleanupTask> implements ReportProgress {
	
	private Set<String> imdbIds = new HashSet<String>();
	
	int totalItems, itemsDone;
	
	@Override
	public int getTotalItems() {
		return totalItems;
	}
	
	@Override
	public int getItemsDone() {
		return itemsDone;
	}
	
	private MovieDAO movieDAO;
	private DownloadableDAO downloadableDAO;

	public MovieCleanupExecutor(MovieCleanupTask task, MovieDAO movieDAO, DownloadableDAO downloadableDAO) {
		super(task);
		this.movieDAO = movieDAO;
		this.downloadableDAO = downloadableDAO;
	}

	@Override
	public void execute() throws Exception {
		
		DownloadableManager.getInstance().delete( Movie.class, DownloadableStatus.IGNORED);

		List<Movie> allDownloadedMovies = movieDAO.findDownloaded();
		totalItems = allDownloadedMovies.size();

		List<Path> movieFolders = MovieManager.getInstance().getFolders();

		for (Movie movie : allDownloadedMovies) {
			Path moviePath = movie.getPath();
			
			if (moviePath == null || Files.isDirectory(moviePath)) {
				downloadableDAO.delete( movie.getId() );
				continue;
			}
			
			Path folder = null;
			for (Path movieFolderPath : movieFolders) {
				if (moviePath.startsWith( movieFolderPath )) {
					folder = movieFolderPath;
					break;
				}
			}
			
			if (folder == null) {
				// this movie is not in any of ours movie folders
				downloadableDAO.delete( movie.getId() );
				continue;
			}

			if (!Files.exists(folder)) {
				// folder is unreachable (maybe just for now)
				continue;
			}
			
			if (moviePath != null && Files.exists( moviePath )) {
				if ( movie.getImdbID() != null ) {
					imdbIds.add( movie.getImdbID() );
				}
				if (task.isRename()) {
					queue(new RenameMovieFileTask( movie ), false);
				}
			}
			
			itemsDone ++;
		}
	}

}
