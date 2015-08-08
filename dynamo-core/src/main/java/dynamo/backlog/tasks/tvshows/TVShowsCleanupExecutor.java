package dynamo.backlog.tasks.tvshows;

import java.nio.file.Path;
import java.util.List;

import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.TVShowDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.tvshows.TVShowManager;
import dynamo.model.tvshows.TVShowSeason;
import model.ManagedSeries;

public class TVShowsCleanupExecutor extends TaskExecutor<TVShowsCleanupTask> {
	
	private TVShowDAO tvShowDAO;

	public TVShowsCleanupExecutor(TVShowsCleanupTask task, TVShowDAO tvShowDAO) {
		super(task);
		this.tvShowDAO = tvShowDAO;
	}

	@Override
	public void execute() throws Exception {
		List<TVShowSeason> seasons = tvShowDAO.findSeasons();
		for (TVShowSeason tvShowSeason : seasons) {
			// fix paths for seasons
			ManagedSeries series = TVShowManager.getInstance().getManagedSeries( tvShowSeason.getSeriesId() );
			Path theoricalFolder = series.getFolder().resolve( String.format(TVShowManager.getInstance().getSeasonFolderPattern(), tvShowSeason.getSeason()) );
			if (tvShowSeason.getPath() == null || theoricalFolder.compareTo(tvShowSeason.getPath()) != 0) {
				DownloadableManager.getInstance().updatePath(tvShowSeason.getId(), theoricalFolder);
			}
		}
	}

}
