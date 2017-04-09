package com.github.dynamo.subtitles;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

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
	
	private ManagedEpisode episode;
	private ManagedSeries series;
	private static Set<EpisodeSubtitlesFinder> finders;
	
	static {
		finders = (Set<EpisodeSubtitlesFinder>) DynamoObjectFactory.getInstances( EpisodeSubtitlesFinder.class );
	}
	
	public FindSubtitleEpisodeExecutor( FindSubtitleEpisodeTask item ) {
		super(item);

		episode = task.getEpisode();
		series = TVShowManager.getInstance().getManagedSeries( episode.getSeriesId() );
	}
	
	@Override
	public RemoteSubTitles downloadSubtitles(Path mainVideoFile, Path subtitlesFile, VideoMetaData metaData, Language language) {
		RemoteSubTitles selectedSubTitles = null;
		for (String seriesName : series.getAllNames()) {
			
			if (isCancelled()) {
				break;
			}
			
			VideoDetails details = new VideoDetails( mainVideoFile, seriesName, episode.getQuality(), episode.getSource(), episode.getReleaseGroup(), episode.getSeasonNumber(), episode.getEpisodeNumber(), metaData.getOpenSubtitlesHash() );		
			
			for (EpisodeSubtitlesFinder subTitleFinder : finders) {
				
				if (isCancelled()) {
					break;
				}
				
				try {
					RemoteSubTitles subtitles = findFromFinder(subTitleFinder, details, series.getSubtitlesLanguage());
					if (subtitles != null && subtitles.getScore() >= 6) {
						selectedSubTitles = subtitles;
					}
				} catch (Exception e) {
					ErrorManager.getInstance().reportThrowable(e);
				}
			}
		}
		return selectedSubTitles;
	}
	
	private RemoteSubTitles findFromFinder( EpisodeSubtitlesFinder subTitleFinder, VideoDetails details, Language subtitlesLanguage ) throws Exception {
		return subTitleFinder.downloadEpisodeSubtitle(details.getName(), details.getSeason(), details.getEpisode(), details.getReleaseGroup(), new Locale(subtitlesLanguage.getShortName()));
	}

	@Override
	public Language getSubtitlesLanguage() {
		return series.getSubtitlesLanguage();
	}


}
