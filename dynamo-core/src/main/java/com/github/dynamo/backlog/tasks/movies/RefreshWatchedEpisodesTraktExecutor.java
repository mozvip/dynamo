package com.github.dynamo.backlog.tasks.movies;

import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.files.DeleteDownloadableTask;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.trakt.TraktManager;
import com.github.dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import com.github.dynamo.tvshows.jdbi.TVShowDAO;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.ManagedSeries;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.uwetrottmann.trakt5.entities.BaseEpisode;
import com.uwetrottmann.trakt5.entities.BaseSeason;
import com.uwetrottmann.trakt5.entities.BaseShow;

public class RefreshWatchedEpisodesTraktExecutor extends TaskExecutor<RefreshWatchedEpisodesTask> {
	
	private ManagedEpisodeDAO managedEpisodeDAO = null;
	private TVShowDAO tvshowDAO = null;

	public RefreshWatchedEpisodesTraktExecutor(RefreshWatchedEpisodesTask item, ManagedEpisodeDAO managedEpisodeDAO, TVShowDAO tvshowDAO) {
		super(item);
		this.managedEpisodeDAO = managedEpisodeDAO;
		this.tvshowDAO = tvshowDAO;
	}

	@Override
	public void execute() throws Exception {
		if (!TVShowManager.getInstance().isEnabled() || !TraktManager.getInstance().isEnabled()) {
			return;
		}
		
		List<BaseShow> watchedEntries = TraktManager.getInstance().getShowsWatched();
		if (watchedEntries != null ) {
			for (BaseShow watchedEntry : watchedEntries) {
				
				String imdbId = watchedEntry.show.ids.imdb;
				
				ManagedSeries series = tvshowDAO.findTVShowByImdbId( imdbId );
				if (series == null) {
					continue;
				}
				
				for (BaseSeason season : watchedEntry.seasons) {
					List<ManagedEpisode> episodes = managedEpisodeDAO.findEpisodesForTVShowAndSeason(series.getId(), season.number);
					for (BaseEpisode episode : season.episodes) {
						for (ManagedEpisode managedEpisode : episodes) {
							if (managedEpisode.getEpisodeNumber() == episode.number) {
								managedEpisodeDAO.setWatched( managedEpisode.getId() ) ;
								if (TVShowManager.getInstance().isDeleteWatched()) {
									if (managedEpisode.getStatus() == DownloadableStatus.DOWNLOADED) {
										BackLogProcessor.getInstance().schedule( new DeleteDownloadableTask( managedEpisode ));
									}
								}
							}
						}
					}
				}
			}
		}
	}


}
