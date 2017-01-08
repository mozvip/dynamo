package com.github.dynamo.backlog.tasks.tvshows;

import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.ScanFolderExecutor;
import com.github.dynamo.backlog.tasks.core.VideoFileFilter;
import com.github.dynamo.backlog.tasks.files.ScanFolderTask;
import com.github.dynamo.core.Language;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import com.github.dynamo.tvshows.jdbi.TVShowDAO;
import com.github.dynamo.tvshows.jdbi.UnrecognizedDAO;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.ManagedSeries;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.omertron.thetvdbapi.model.Series;

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
