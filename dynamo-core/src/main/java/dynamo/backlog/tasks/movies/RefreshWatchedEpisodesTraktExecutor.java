package dynamo.backlog.tasks.movies;

import java.util.List;

import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.TVShowDAO;
import dynamo.model.DownloadableStatus;
import dynamo.model.tvshows.TVShowManager;
import dynamo.trakt.TraktManager;
import dynamo.trakt.TraktShowEpisode;
import dynamo.trakt.TraktShowSeason;
import dynamo.trakt.TraktWatchedEntry;
import model.ManagedEpisode;
import model.ManagedSeries;

public class RefreshWatchedEpisodesTraktExecutor extends TaskExecutor<RefreshWatchedEpisodesTask> {
	
	private TVShowDAO tvShowDAO = null;

	public RefreshWatchedEpisodesTraktExecutor(RefreshWatchedEpisodesTask item, TVShowDAO tvShowDAO) {
		super(item);
		this.tvShowDAO = tvShowDAO;
	}

	@Override
	public void execute() throws Exception {
		if (!TVShowManager.getInstance().isEnabled() || !TraktManager.getInstance().isEnabled()) {
			return;
		}
		
		List<TraktWatchedEntry> watchedEntries = TraktManager.getInstance().getShowsWatched();
		if (watchedEntries != null ) {
			for (TraktWatchedEntry watchedEntry : watchedEntries) {
				
				String imdbId = watchedEntry.getShow().getIds().get("imdb");
				
				ManagedSeries series = tvShowDAO.findTVShowByImdbId( imdbId );
				if (series == null) {
					continue;
				}
				for (TraktShowSeason season : watchedEntry.getSeasons()) {
					List<ManagedEpisode> episodes = tvShowDAO.findEpisodesForTVShowAndSeason(series.getId(), season.getNumber());
					for (TraktShowEpisode episode : season.getEpisodes()) {
						for (ManagedEpisode managedEpisode : episodes) {
							if (managedEpisode.getEpisodeNumber() == episode.getNumber()) {
								tvShowDAO.setWatched( managedEpisode.getId() ) ;
								if (TVShowManager.getInstance().isDeleteWatched()) {
									if (managedEpisode.getStatus() == DownloadableStatus.DOWNLOADED) {
										queue( new DeleteDownloadableTask( managedEpisode ));
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
