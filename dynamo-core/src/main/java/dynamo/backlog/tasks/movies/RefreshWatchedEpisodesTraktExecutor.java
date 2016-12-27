package dynamo.backlog.tasks.movies;

import java.util.List;

import com.uwetrottmann.trakt5.entities.BaseEpisode;
import com.uwetrottmann.trakt5.entities.BaseSeason;
import com.uwetrottmann.trakt5.entities.BaseShow;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.core.model.TaskExecutor;
import dynamo.model.DownloadableStatus;
import dynamo.trakt.TraktManager;
import dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import dynamo.tvshows.jdbi.TVShowDAO;
import dynamo.tvshows.model.ManagedEpisode;
import dynamo.tvshows.model.ManagedSeries;
import dynamo.tvshows.model.TVShowManager;

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
