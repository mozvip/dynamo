package com.github.dynamo.backlog.tasks.movies;

import java.util.List;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.trakt.TraktManager;
import com.uwetrottmann.trakt5.entities.BaseMovie;

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
