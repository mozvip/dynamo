package dynamo.backlog.tasks.tvshows;

import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.omertron.thetvdbapi.model.Series;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.ScanFolderExecutor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.backlog.tasks.files.ScanFolderTask;
import dynamo.core.Language;
import dynamo.model.DownloadableStatus;
import dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import dynamo.tvshows.jdbi.TVShowDAO;
import dynamo.tvshows.jdbi.UnrecognizedDAO;
import dynamo.tvshows.model.ManagedEpisode;
import dynamo.tvshows.model.ManagedSeries;
import dynamo.tvshows.model.TVShowManager;

public class NewTVShowFolderExecutor extends ScanFolderExecutor<ScanFolderTask> {
	
	private TVShowDAO tvShowDAO;
	private ManagedEpisodeDAO managedEpisodeDAO;
	private UnrecognizedDAO unrecognizedDAO;

	public NewTVShowFolderExecutor( NewTVShowFolderTask task, TVShowDAO tvShowDAO, ManagedEpisodeDAO managedEpisodeDAO, UnrecognizedDAO unrecognizedDAO ) {
		super(task);
		this.tvShowDAO = tvShowDAO;
		this.managedEpisodeDAO = managedEpisodeDAO;
		this.unrecognizedDAO = unrecognizedDAO;
	}
	


	@Override
	public void parsePath(Path folder) throws Exception {
		
		Language metaDataLanguage = TVShowManager.getInstance().getMetaDataLanguage();
		Language audioLang = TVShowManager.getInstance().getAudioLanguage();
		Language subLang = TVShowManager.getInstance().getSubtitlesLanguage();

		ManagedSeries managed = tvShowDAO.getTVShowForFolder( folder );

		if (managed == null) {

			String name = folder.getFileName().toString();

			Series tvdbSeries = TVShowManager.getInstance().searchTVShow( name );
			if (tvdbSeries != null) {
				managed = TVShowManager.getInstance().newSeries( tvdbSeries, folder, metaDataLanguage, audioLang, subLang );
			} else {

				if ( unrecognizedDAO.findUnrecognizedFolder( folder ) == null) {
					unrecognizedDAO.createUnrecognizedFolder( folder );
				}				
			}

		} else {

			boolean mustRefresh = !managed.isEnded(); 
			LocalDateTime nextRefreshDate = LocalDateTime.now().plusMonths(1);

			List<ManagedEpisode> futureEpisodes = managedEpisodeDAO.findEpisodesForTVShowAndStatus( managed.getId(), DownloadableStatus.FUTURE );
			if (futureEpisodes != null && futureEpisodes.size() > 0) {
				mustRefresh = true;
				nextRefreshDate = null;
				for (ManagedEpisode managedEpisode : futureEpisodes) {
					if (managedEpisode.getFirstAired() == null) {
						nextRefreshDate = null;
						break;
					} else if ( nextRefreshDate == null || managedEpisode.getFirstAired().isBefore( nextRefreshDate.toLocalDate() )) {
						nextRefreshDate = LocalDateTime.of( managedEpisode.getFirstAired(), LocalTime.now() );
					}
				}
			}

			if (mustRefresh) {
				BackLogProcessor.getInstance().schedule( new RefreshFromTVDBTask( managed ), nextRefreshDate, false );
			}
		}
	}

	@Override
	public Filter<Path> getFileFilter() {
		return VideoFileFilter.getInstance();
	}


}
