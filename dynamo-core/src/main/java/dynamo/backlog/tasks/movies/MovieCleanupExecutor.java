package dynamo.backlog.tasks.movies;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.MovieDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;
import dynamo.video.VideoManager;

public class MovieCleanupExecutor extends TaskExecutor<MovieCleanupTask> implements ReportProgress {
	
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
		
		Set<Path> existingFolders = new HashSet<>();
		
		itemsDone = -1;

		for (Movie movie : allDownloadedMovies) {
			
			itemsDone ++;
			
			Optional<Path> mainVideoFile = VideoManager.getInstance().getMainVideoFile( movie.getId() );
			if (mainVideoFile.isPresent()) {
				Path moviePath = mainVideoFile.get();
	
				Path folder = null;
				for (Path movieFolderPath : movieFolders) {
					if (moviePath.startsWith( movieFolderPath )) {
						folder = movieFolderPath;
						break;
					}
				}
				
				if (folder == null) {
					// this movie is not in any of our movies folders
					DownloadableManager.getInstance().delete( movie.getId() );
					continue;
				}
	
				if (!existingFolders.contains( folder )) {
					if (!Files.exists(folder)) {
						// folder is unreachable (maybe just for now)
						continue;
					} else {
						existingFolders.add( folder );
					}
				}
				
				if (Files.exists( moviePath ) && task.isRename()) {
					queue(new RenameMovieFileTask( movie ), false);
				}
			} else {

				downloadableDAO.delete( movie.getId() );
				
			}
		}
	}

}
