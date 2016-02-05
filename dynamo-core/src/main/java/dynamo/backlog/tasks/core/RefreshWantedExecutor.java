package dynamo.backlog.tasks.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dynamo.core.manager.DownloadableFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadInfo;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.tvshows.TVShowManager;
import dynamo.model.tvshows.TVShowSeason;
import model.ManagedEpisode;

public class RefreshWantedExecutor extends TaskExecutor<RefreshWantedTask> {

	public RefreshWantedExecutor(RefreshWantedTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {

		List<DownloadInfo> infos = DownloadableManager.getInstance().findWanted();
		List<ManagedEpisode> episodes = new ArrayList<>();

		for (DownloadInfo downloadInfo : infos) {
			Downloadable downloadable;
			try {
				downloadable = DownloadableFactory.getInstance().createInstance( downloadInfo.getId(), downloadInfo.getDownloadableClass() );
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( task, e );
				continue;
			}
			
			if (downloadable == null) {
				try {
					DownloadableManager.getInstance().delete(downloadInfo.getId());
				} catch (Exception e) {
					ErrorManager.getInstance().reportThrowable( task, e );
				}
				continue;
			}
			
			if (downloadable instanceof ManagedEpisode) {
				// episodes need special treatment : they can be grouped in full seasons
				episodes.add( (ManagedEpisode) downloadable );
			} else {
				if (!(downloadable instanceof TVShowSeason)) {
					DownloadableManager.getInstance().scheduleFind( downloadable );
				}
			}
		}
		
		Set<Long> checkedSeasonIds = new HashSet<>();
		Set<Long> scheduledSeasonIds = new HashSet<>();

		for (ManagedEpisode managedEpisode : episodes) {
			boolean wholeSeasonWanted = false;
			if (!checkedSeasonIds.contains( managedEpisode.getSeasonId())) {
				List<ManagedEpisode> episodesForSeason = TVShowManager.getInstance().findEpisodesForSeason( managedEpisode.getSeasonId() );
				wholeSeasonWanted = episodesForSeason.stream().allMatch( episode -> episode.getStatus() == DownloadableStatus.WANTED );
			}
			
			checkedSeasonIds.add( managedEpisode.getSeasonId() );
			
			if (wholeSeasonWanted && !scheduledSeasonIds.contains( managedEpisode.getSeasonId() )) {
				DownloadableManager.getInstance().scheduleFind( TVShowManager.getInstance().findSeason( managedEpisode.getSeasonId() ) );
				scheduledSeasonIds.add( managedEpisode.getSeasonId() );
			} else {
				DownloadableManager.getInstance().scheduleFind( managedEpisode );
			}
		}
		
		
	}

}
