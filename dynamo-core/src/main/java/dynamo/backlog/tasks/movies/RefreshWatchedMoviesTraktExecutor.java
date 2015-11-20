package dynamo.backlog.tasks.movies;

import java.util.List;

import dynamo.core.model.TaskExecutor;
import dynamo.model.DownloadableStatus;
import dynamo.model.movies.MovieManager;
import dynamo.trakt.TraktManager;
import dynamo.trakt.TraktWatchedEntry;

public class RefreshWatchedMoviesTraktExecutor extends TaskExecutor<RefreshWatchedMoviesTask> {

	public RefreshWatchedMoviesTraktExecutor(RefreshWatchedMoviesTask item) {
		super(item);
	}

	@Override
	public void execute() throws Exception {
		if (!MovieManager.getInstance().isEnabled() || !TraktManager.getInstance().isEnabled()) {
			return;
		}
		List<TraktWatchedEntry> watchedMovies = TraktManager.getInstance().getMoviesWatched();
		if (watchedMovies != null ) {
			for (TraktWatchedEntry watched : watchedMovies) {
				String imdbId = watched.getMovie().getIds().get("imdb");
				
				MovieManager.getInstance().createByImdbID( imdbId, null, null, DownloadableStatus.IGNORED );
			}
		}
	}


}
