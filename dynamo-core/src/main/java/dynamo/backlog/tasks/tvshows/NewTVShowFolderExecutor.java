package dynamo.backlog.tasks.tvshows;

import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.omertron.thetvdbapi.model.Series;

import dynamo.backlog.tasks.core.AbstractNewFolderExecutor;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.core.Language;
import dynamo.jdbi.TVShowDAO;
import dynamo.manager.LocalImageCache;
import dynamo.model.DownloadableStatus;
import dynamo.model.tvshows.TVShowManager;
import liquibase.sqlgenerator.core.GetViewDefinitionGeneratorMSSQL;
import model.ManagedEpisode;
import model.ManagedSeries;
import model.backlog.NewTVShowFolderTask;
import model.backlog.RefreshTVShowTask;

public class NewTVShowFolderExecutor extends AbstractNewFolderExecutor<NewTVShowFolderTask> {
	
	private TVShowDAO tvShowDAO;

	public NewTVShowFolderExecutor( NewTVShowFolderTask item, TVShowDAO tvShowDAO ) {
		super(item);
		this.tvShowDAO = tvShowDAO;
	}
	
	@Override
	public void parsePath(Path folder) throws Exception {
		
		Language metaDataLanguage = TVShowManager.getInstance().getMetaDataLanguage();
		Language audioLang = TVShowManager.getInstance().getAudioLanguage();
		Language subLang = TVShowManager.getInstance().getSubtitlesLanguage();

		ManagedSeries managed = tvShowDAO.getTVShowForFolder( folder );

		if (managed == null) {
			String name = folder.getFileName().toString();

			Series tvdbSeries = TVShowManager.getInstance().searchTVShow( name );;
			if (tvdbSeries != null) {
				managed = TVShowManager.getInstance().newSeries( tvdbSeries, folder, metaDataLanguage, audioLang, subLang );
			} else {

				if ( tvShowDAO.findUnrecognizedFolder( folder ) == null) {
					tvShowDAO.createUnrecognizedFolder( folder );
				}				
			}
		} else {
			
			boolean mustRefresh = !managed.isEnded(); 
			Date nextRefreshDate = null;

			if (managed.getBanner() == null || LocalImageCache.getInstance().missFile( managed.getBanner() ) ) {
				mustRefresh = true;
			} else if ( managed.getPoster() == null || LocalImageCache.getInstance().missFile( managed.getPoster() ) ) {
				mustRefresh = true;
			} else {
				
				Calendar nextMonthCal = Calendar.getInstance();
				nextMonthCal.add(Calendar.MONTH, 1);
				nextRefreshDate = nextMonthCal.getTime();

				List<ManagedEpisode> futureEpisodes = tvShowDAO.findEpisodesForTVShowAndStatus( managed.getId(), DownloadableStatus.FUTURE );
				if (futureEpisodes != null && futureEpisodes.size() > 0) {
					mustRefresh = true;
					nextRefreshDate = null;
					for (ManagedEpisode managedEpisode : futureEpisodes) {
						if (managedEpisode.getFirstAired() == null) {
							nextRefreshDate = null;
							break;
						} else if ( nextRefreshDate == null || managedEpisode.getFirstAired().before( nextRefreshDate )) {
							nextRefreshDate = managedEpisode.getFirstAired();
						}
					}
				}
			}

			if (mustRefresh) {
				RefreshTVShowTask task = new RefreshTVShowTask( managed.getId() );
				task.setMinDate( nextRefreshDate );
				queue( task, false );
			}
		}
	}

	@Override
	public Filter<Path> getFileFilter() {
		return VideoFileFilter.getInstance();
	}


}
