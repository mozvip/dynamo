package dynamo.backlog.tasks.movies;

import java.util.List;

import com.uwetrottmann.trakt5.entities.BaseMovie;

import dynamo.core.Language;
import dynamo.core.model.TaskExecutor;
import dynamo.model.DownloadableStatus;
import dynamo.movies.model.MovieManager;
import dynamo.trakt.TraktManager;

public class RefreshWatchedMoviesTraktExecutor extends TaskExecutor<RefreshWatchedMoviesTask> {

	public RefreshWatchedMoviesTraktExecutor(RefreshWatchedMoviesTask item) {
		super(item);
	}

	@Override
	public void execute() throws Exception {
		if (!MovieManager.getInstance().isEnabled() || !TraktManager.getInstance().isEnabled()) {
			return;
		}
		List<BaseMovie> watchedMovies = TraktManager.getInstance().getMoviesWatched();
		if (watchedMovies != null ) {
			for (BaseMovie watched : watchedMovies) {
				String imdbId = watched.movie.ids.imdb;
				MovieManager.getInstance().createByImdbID( imdbId, null, Language.EN, DownloadableStatus.IGNORED, true );
			}
		}
	}


}
