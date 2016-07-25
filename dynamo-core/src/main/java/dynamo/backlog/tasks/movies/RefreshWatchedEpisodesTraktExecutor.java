package dynamo.backlog.tasks.movies;

import java.util.List;

import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.core.model.TaskExecutor;
import dynamo.model.DownloadableStatus;
import dynamo.model.tvshows.TVShowManager;
import dynamo.trakt.TraktManager;
import dynamo.trakt.TraktShowEpisode;
import dynamo.trakt.TraktShowSeason;
import dynamo.trakt.TraktWatchedEntry;
import dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import dynamo.tvshows.jdbi.TVShowDAO;
import model.ManagedEpisode;
import model.ManagedSeries;

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
		
		List<TraktWatchedEntry> watchedEntries = TraktManager.getInstance().getShowsWatched();
		if (watchedEntries != null ) {
			for (TraktWatchedEntry watchedEntry : watchedEntries) {
				
				String imdbId = watchedEntry.getShow().getIds().get("imdb");
				
				ManagedSeries series = tvshowDAO.findTVShowByImdbId( imdbId );
				if (series == null) {
					continue;
				}
				for (TraktShowSeason season : watchedEntry.getSeasons()) {
					List<ManagedEpisode> episodes = managedEpisodeDAO.findEpisodesForTVShowAndSeason(series.getId(), season.getNumber());
					for (TraktShowEpisode episode : season.getEpisodes()) {
						for (ManagedEpisode managedEpisode : episodes) {
							if (managedEpisode.getEpisodeNumber() == episode.getNumber()) {
								managedEpisodeDAO.setWatched( managedEpisode.getId() ) ;
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
