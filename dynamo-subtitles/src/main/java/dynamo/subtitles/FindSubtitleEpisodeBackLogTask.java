package dynamo.subtitles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.EventManager;
import dynamo.core.model.HistoryDAO;
import dynamo.core.model.TaskExecutor;
import dynamo.core.model.video.VideoMetaData;
import dynamo.jdbi.TVShowDAO;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.subtitles.FindSubtitleEpisodeTask;
import dynamo.model.tvshows.TVShowManager;
import dynamo.video.VideoManager;
import model.ManagedEpisode;
import model.ManagedSeries;

public class FindSubtitleEpisodeBackLogTask extends TaskExecutor<FindSubtitleEpisodeTask> {
	
	private HistoryDAO historyDAO;
	private TVShowDAO tvShowDAO;
	
	private ManagedEpisode episode;
	private ManagedSeries series;
	
	public FindSubtitleEpisodeBackLogTask( FindSubtitleEpisodeTask item, TVShowDAO tvShowDAO, HistoryDAO historyDAO ) {
		super(item);
		this.tvShowDAO = tvShowDAO;
		this.historyDAO = historyDAO;

		episode = task.getEpisode();
		series = TVShowManager.getInstance().getManagedSeries( episode.getSeriesId() );
	}

	@Override
	public void execute() throws Exception {
		
		if (episode.getPath() == null || !Files.isRegularFile( episode.getPath()) || episode.isSubtitled() || series.getSubtitleLanguage() == null) {
			return;
		}

		VideoMetaData metaData = VideoManager.getInstance().getMetaData(episode, episode.getPath());
		if (metaData.getSubtitleLanguages() != null && metaData.getSubtitleLanguages().contains( series.getSubtitleLanguage() )) {
			tvShowDAO.setSubtitled(episode.getId());
			return;
		}

		for (String name : series.getAka()) {

			Path subtitles = SubTitleDownloader.getInstance().downloadSubTitle(
					episode,
					name,
					episode.getQuality(),
					episode.getSource(),
					episode.getReleaseGroup(),
					episode.getSeasonNumber(), episode.getEpisodeNumber(),
					series.getSubtitleLanguage() );
			
			if ( subtitles != null ) {
				
				String filename = episode.getPath().getFileName().toString();
				String filenameWithoutExtension = filename; 
				if ( filenameWithoutExtension.lastIndexOf('.') > 0 ) {
					filenameWithoutExtension = filenameWithoutExtension.substring( 0, filenameWithoutExtension.lastIndexOf('.'));
				}
				
				Path destinationSRT = episode.getPath().getParent().resolve( filenameWithoutExtension + ".srt" );
				
				Files.move( subtitles, destinationSRT, StandardCopyOption.REPLACE_EXISTING);

				tvShowDAO.updateSubtitlesPath( episode.getId(), destinationSRT );
				
				String message = String.format("Subtitles for <a href='%s'>%s</a> have been found", episode.getRelativeLink(), episode.toString());
				historyDAO.insert( message, DownloadableStatus.SUBTITLED, episode.getId() );
				EventManager.getInstance().reportSuccess( message );

				break;				
			}

		}

		
	}
	
	@Override
	public void rescheduleTask(FindSubtitleEpisodeTask item) {
		ManagedEpisode episode = item.getEpisode();
		if ( !TVShowManager.getInstance().isAlreadySubtitled( episode, series.getSubtitleLanguage() )) {
			item.setMinDate( getNextDate( 60 * 24 ) );
			BackLogProcessor.getInstance().schedule( item, false );
		}
	}

}
