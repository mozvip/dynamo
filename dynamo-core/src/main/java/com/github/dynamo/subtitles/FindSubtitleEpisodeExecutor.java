package com.github.dynamo.subtitles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.EventManager;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoDetails;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.HistoryDAO;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.core.model.video.VideoMetaData;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.backlog.subtitles.FindSubtitleEpisodeTask;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.ManagedSeries;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.github.dynamo.video.VideoManager;
import com.github.mozvip.subtitles.EpisodeSubtitlesFinder;
import com.github.mozvip.subtitles.RemoteSubTitles;

public class FindSubtitleEpisodeExecutor extends TaskExecutor<FindSubtitleEpisodeTask> {
	
	private HistoryDAO historyDAO;
	
	private ManagedEpisode episode;
	private ManagedSeries series;
	private static Set<EpisodeSubtitlesFinder> finders;
	
	static {
		finders = (Set<EpisodeSubtitlesFinder>) DynamoObjectFactory.getInstances( EpisodeSubtitlesFinder.class );
	}
	
	public FindSubtitleEpisodeExecutor( FindSubtitleEpisodeTask item, HistoryDAO historyDAO ) {
		super(item);
		this.historyDAO = historyDAO;

		episode = task.getEpisode();
		series = TVShowManager.getInstance().getManagedSeries( episode.getSeriesId() );
	}
	
	private RemoteSubTitles findFromFinder( EpisodeSubtitlesFinder subTitleFinder, VideoDetails details, Language subtitlesLanguage ) throws Exception {
		return subTitleFinder.downloadEpisodeSubtitle(details.getName(), details.getSeason(), details.getEpisode(), details.getReleaseGroup(), new Locale(subtitlesLanguage.getShortName()));
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
			
			if (isCancelled()) {
				break;
			}

			String filename = mainVideoFilePath.getFileName().toString();
			String filenameWithoutExtension = filename; 
			if ( filenameWithoutExtension.lastIndexOf('.') > 0 ) {
				filenameWithoutExtension = filenameWithoutExtension.substring( 0, filenameWithoutExtension.lastIndexOf('.'));
			}
			
			Path destinationSRT = mainVideoFilePath.getParent().resolve( filenameWithoutExtension + ".srt" );
			
			VideoDetails details = new VideoDetails( mainVideoFilePath, seriesName, episode.getQuality(), episode.getSource(), episode.getReleaseGroup(), episode.getSeasonNumber(), episode.getEpisodeNumber(), metaData.getOpenSubtitlesHash() );		
			RemoteSubTitles selectedSubTitles = null;
			
			for (EpisodeSubtitlesFinder subTitleFinder : finders) {
				
				if (isCancelled()) {
					break;
				}
				
				selectedSubTitles = findFromFinder(subTitleFinder, details, series.getSubtitlesLanguage());
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
