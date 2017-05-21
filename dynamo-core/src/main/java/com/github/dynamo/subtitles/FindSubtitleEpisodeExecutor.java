package com.github.dynamo.subtitles;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoDetails;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.video.VideoMetaData;
import com.github.dynamo.tvshows.model.ManagedEpisode;
import com.github.dynamo.tvshows.model.ManagedSeries;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.github.mozvip.subtitles.EpisodeSubtitlesFinder;
import com.github.mozvip.subtitles.RemoteSubTitles;

public class FindSubtitleEpisodeExecutor extends AbstractFindSubtitlesExecutor<FindSubtitleEpisodeTask> {
	
	private static Set<EpisodeSubtitlesFinder> finders;
	static {
		finders = (Set<EpisodeSubtitlesFinder>) DynamoObjectFactory.getInstances( EpisodeSubtitlesFinder.class );
	}

	private ManagedEpisode episode;
	private ManagedSeries series;
	
	public FindSubtitleEpisodeExecutor( FindSubtitleEpisodeTask item ) {
		super(item);

		episode = task.getEpisode();
		series = TVShowManager.getInstance().getManagedSeries( episode.getSeriesId() );
	}
	
	@Override
	public RemoteSubTitles downloadSubtitles(Path mainVideoFile, Path subtitlesFile, VideoMetaData metaData, Language language) {

		List<Future<RemoteSubTitles>> futures = new ArrayList<>();
		for (String seriesName : series.getAllNames()) {
			if (isCancelled()) {
				break;
			}
			VideoDetails details = new VideoDetails( mainVideoFile, seriesName, episode.getQuality(), episode.getSource(), episode.getReleaseGroup(), episode.getSeasonNumber(), episode.getEpisodeNumber(), metaData.getOpenSubtitlesHash() );		
			for (EpisodeSubtitlesFinder subTitleFinder : finders) {
				if (isCancelled()) {
					break;
				}
				futures.add( findFromFinder(subTitleFinder, details, series.getSubtitlesLanguage()) );
			}
		}
		if (isCancelled()) {
			return null;
		}

		RemoteSubTitles selectedSubTitles = null;
		int currentScore = -1;
		for (Future<RemoteSubTitles> future : futures) {
			RemoteSubTitles remoteSubTitles;
			try {
				remoteSubTitles = future.get();
				if (remoteSubTitles != null && remoteSubTitles.getScore() > currentScore) {
					currentScore = remoteSubTitles.getScore();
					selectedSubTitles = remoteSubTitles;
				}
			} catch (InterruptedException | ExecutionException e) {
				ErrorManager.getInstance().reportThrowable(getTask(), e);
			}
		}
		return selectedSubTitles;
	}

	private Future<RemoteSubTitles> findFromFinder( EpisodeSubtitlesFinder subTitleFinder, VideoDetails details, Language subtitlesLanguage ) {
		ExecutorService service = services.get( subTitleFinder.getClass().getName() );
		return service.submit( new Callable<RemoteSubTitles>() {
			@Override
			public RemoteSubTitles call() throws Exception {
				return subTitleFinder.downloadEpisodeSubtitle(details.getName(), details.getSeason(), details.getEpisode(), details.getReleaseGroup(), new Locale(subtitlesLanguage.getShortName()));
			}
		});
	}

	@Override
	public Language getSubtitlesLanguage() {
		return series.getSubtitlesLanguage();
	}


}
