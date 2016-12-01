package dynamo.subtitles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Set;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.EventManager;
import dynamo.core.Language;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.HistoryDAO;
import dynamo.core.model.TaskExecutor;
import dynamo.core.model.video.VideoMetaData;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.subtitles.FindSubtitleEpisodeTask;
import dynamo.tvshows.model.ManagedEpisode;
import dynamo.tvshows.model.ManagedSeries;
import dynamo.tvshows.model.TVShowManager;
import dynamo.video.VideoManager;

public class FindSubtitleEpisodeExecutor extends TaskExecutor<FindSubtitleEpisodeTask> {
	
	private HistoryDAO historyDAO;
	
	private ManagedEpisode episode;
	private ManagedSeries series;
	private static Set<SubtitlesFinder> finders;
	
	static {
		finders = (Set<SubtitlesFinder>) DynamoObjectFactory.getInstances( SubtitlesFinder.class );
	}
	
	
	public FindSubtitleEpisodeExecutor( FindSubtitleEpisodeTask item, HistoryDAO historyDAO ) {
		super(item);
		this.historyDAO = historyDAO;

		episode = task.getEpisode();
		series = TVShowManager.getInstance().getManagedSeries( episode.getSeriesId() );
	}
	
	private RemoteSubTitles findFromFinder( SubtitlesFinder subTitleFinder, VideoDetails details, Language subtitlesLanguage ) throws Exception {
		return subTitleFinder.findSubtitles( details, subtitlesLanguage );
	}

	@Override
	public void execute() throws Exception {
		
		Optional<Path> mainVideoFile = VideoManager.getInstance().getMainVideoFile( episode.getId() );
		Path mainVideoFilePath = mainVideoFile.get();
		if (!mainVideoFile.isPresent() || !Files.isRegularFile( mainVideoFilePath)) {
			ErrorManager.getInstance().reportWarning( String.format( "Unable to download subtitles for %s : video file not present", episode.getName() ));
			return;
		}

		if (series.getSubtitlesLanguage() == null || VideoManager.isAlreadySubtitled(episode, series.getSubtitlesLanguage())) {
			return;
		}
		
		VideoMetaData metaData = VideoManager.getInstance().getMetaData(episode, mainVideoFilePath );

		for (String seriesName : series.getAllNames()) {
			
			String filename = mainVideoFilePath.getFileName().toString();
			String filenameWithoutExtension = filename; 
			if ( filenameWithoutExtension.lastIndexOf('.') > 0 ) {
				filenameWithoutExtension = filenameWithoutExtension.substring( 0, filenameWithoutExtension.lastIndexOf('.'));
			}
			
			Path destinationSRT = mainVideoFilePath.getParent().resolve( filenameWithoutExtension + ".srt" );
			
			VideoDetails details = new VideoDetails( mainVideoFilePath, seriesName, episode.getQuality(), episode.getSource(), episode.getReleaseGroup(), episode.getSeasonNumber(), episode.getEpisodeNumber(), metaData.getOpenSubtitlesHash() );		
			RemoteSubTitles selectedSubTitles = null;
			
			for (SubtitlesFinder subTitleFinder : finders) {
				if (subTitleFinder.isEnabled()) {
					selectedSubTitles = findFromFinder(subTitleFinder, details, series.getSubtitlesLanguage());
				}
				if (selectedSubTitles != null && selectedSubTitles.getScore() >= 6) {
					Files.write(destinationSRT, selectedSubTitles.getData(), StandardOpenOption.CREATE);

					String message = String.format("Subtitles for <a href='%s'>%s</a> have been found", episode.getRelativeLink(), episode.toString());
					historyDAO.insert( message, DownloadableStatus.SUBTITLED, episode.getId() );
					EventManager.getInstance().reportSuccess( message );
					
					// add subtitles to the list of files for this downloadable
					DownloadableManager.getInstance().addFile( episode, destinationSRT, 1 );

					break;				
				}
			}
		}
	}

	@Override
	public void rescheduleTask(FindSubtitleEpisodeTask task) {
		ManagedEpisode episode = task.getEpisode();
		try {
			if ( !VideoManager.isAlreadySubtitled( episode, series.getSubtitlesLanguage() )) {
				BackLogProcessor.getInstance().schedule(task, getNextDate( 60 * 24 ), false);
			}
		} catch (IOException | InterruptedException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}

}
