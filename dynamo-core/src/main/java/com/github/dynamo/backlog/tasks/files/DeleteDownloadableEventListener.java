package com.github.dynamo.backlog.tasks.files;

import java.util.EventListener;
import java.util.List;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.Video;
import com.github.dynamo.model.backlog.find.FindSeasonTask;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.movies.model.Movie;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.google.common.eventbus.Subscribe;

public class DeleteDownloadableEventListener implements EventListener {
	
	private DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );

	@Subscribe
	public void execute( DeleteDownloadableEvent event ) {
		Downloadable downloadable = event.getDownloadable();	

		List<DownloadableFile> allFiles = downloadableDAO.getAllFiles(downloadable.getId());
		for (DownloadableFile downloadableFile : allFiles) {
			BackLogProcessor.getInstance().post( new DeleteEvent( downloadableFile.getFilePath(), true ) );
		}

		// unschedule any associated tasks
		if (downloadable.getStatus() == DownloadableStatus.SNATCHED) {
			DownloadableManager.getInstance().cancelDownload( downloadable.getId() );
		}
		BackLogProcessor.getInstance().unschedule( String.format( "task.downloadable.id == %d", downloadable.getId() ) );

		// FIXME: create sub classes for the different downloadable types
		if ( downloadable instanceof Video ) {
			if (downloadable instanceof ManagedEpisode) {
				ManagedEpisode episode = (ManagedEpisode) downloadable;
				
				episode.setReleaseGroup( null );
				episode.setSource( null );
				episode.setQuality( null );
				
				TVShowManager.getInstance().saveEpisode(episode);
				
				BackLogProcessor.getInstance().unschedule( FindSeasonTask.class, String.format("task.downloadable.series.id == %s and task.downloadable.season == %d", episode.getSeriesId(), episode.getSeasonNumber()) );
			}
		}
		
		downloadableDAO.updateStatus( downloadable.getId(), DownloadableStatus.IGNORED );
		downloadableDAO.updateLabel( downloadable.getId(), null );

		if (downloadable instanceof Movie || downloadable instanceof VideoGame || downloadable instanceof MusicAlbum) {
			DownloadableManager.getInstance().delete( downloadable.getId() );
		}
	}

}
