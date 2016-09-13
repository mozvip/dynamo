package dynamo.backlog.tasks.files;

import java.util.List;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.core.model.LogSuccess;
import dynamo.core.model.TaskExecutor;
import dynamo.games.model.VideoGame;
import dynamo.manager.DownloadableManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.Video;
import dynamo.model.backlog.find.FindSeasonTask;
import dynamo.model.music.MusicAlbum;
import dynamo.model.tvshows.TVShowManager;
import dynamo.movies.model.Movie;
import model.ManagedEpisode;

public class DeleteDownloadableExecutor extends TaskExecutor<DeleteDownloadableTask> implements LogSuccess {
	
	private DownloadableUtilsDAO downloadableDAO;

	public DeleteDownloadableExecutor( DeleteDownloadableTask task, DownloadableUtilsDAO downloadableDAO ) {
		super(task);
		this.downloadableDAO = downloadableDAO;
	}

	@Override
	public void execute() throws Exception {
		Downloadable downloadable = task.getDownloadable();	

		List<DownloadableFile> allFiles = downloadableDAO.getAllFiles(downloadable.getId());
		for (DownloadableFile downloadableFile : allFiles) {
			queue( new DeleteTask( downloadableFile.getFilePath(), true ), false );
		}

		// unschedule any associated tasks
		if (downloadable.getStatus() == DownloadableStatus.SNATCHED) {
			DownloadableManager.getInstance().cancelDownload( downloadable.getId() );
		}
		BackLogProcessor.getInstance().unschedule( String.format( "this.downloadable.id == %d", downloadable.getId() ) );

		// FIXME: create sub classes for the different downloadable types
		if ( downloadable instanceof Video ) {
			Video subtitled = (Video) downloadable;
			if (subtitled.getSubtitlesPath() != null) {
				queue( new DeleteTask( subtitled.getSubtitlesPath(), true ), false );
				((Video) downloadable).setSubtitlesPath( null );
			}

			if (downloadable instanceof ManagedEpisode) {
				ManagedEpisode episode = (ManagedEpisode) downloadable;
				
				episode.setReleaseGroup( null );
				episode.setSource( null );
				episode.setQuality( null );
				episode.setSubtitled( false );
				
				TVShowManager.getInstance().saveEpisode(episode);
				
				BackLogProcessor.getInstance().unschedule( FindSeasonTask.class, String.format("this.downloadable.series.id == %s and this.downloadable.season == %d", episode.getSeriesId(), episode.getSeasonNumber()) );
			}
		}
		
		downloadableDAO.updateStatus( downloadable.getId(), DownloadableStatus.IGNORED );
		downloadableDAO.updateLabel( downloadable.getId(), null );

		if (downloadable instanceof Movie || downloadable instanceof VideoGame || downloadable instanceof MusicAlbum) {
			DownloadableManager.getInstance().delete( downloadable.getId() );
		}
	}

}
