package dynamo.subtitles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import dynamo.core.Language;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.video.VideoMetaData;
import dynamo.model.Downloadable;
import dynamo.video.VideoManager;

public class SubTitleDownloader implements Reconfigurable {
	
	@Configurable(category="Subtitles Finders", name="Enable Subtitles")
	private boolean enabled;
	
	private Set<SubtitlesFinder> finders;
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	static class SingletonHolder {
		private SingletonHolder() {}
		static SubTitleDownloader instance = new SubTitleDownloader();
	}

	public static SubTitleDownloader getInstance() {
		return SingletonHolder.instance;
	}

	private SubTitleDownloader() {
	}
	
	@Override
	public void reconfigure() {
		try {
			finders = new DynamoObjectFactory<SubtitlesFinder>("dynamo", SubtitlesFinder.class).getInstances();
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}

	public Path downloadSubTitle( Downloadable video, Path videoFile, String seriesName, VideoQuality quality, VideoSource source, String releaseGroup, int season, int episode, Language subtitlesLanguage ) throws Exception {

		
		VideoMetaData metaData = VideoManager.getInstance().getMetaData(video, videoFile );
		
		VideoDetails details = new VideoDetails( videoFile, seriesName, quality, source, releaseGroup, season, episode, metaData.getOpenSubtitlesHash() );
		
		RemoteSubTitles selectedSubTitles = null;
		for (SubtitlesFinder subTitleFinder : finders) {
			
			if (!subTitleFinder.isEnabled()) {
				continue;
			}
							
			RemoteSubTitles remoteSubTitles = subTitleFinder.findSubtitles( details, subtitlesLanguage );
			if (remoteSubTitles != null) {
				if (selectedSubTitles == null || ( selectedSubTitles != null && remoteSubTitles.getScore() > selectedSubTitles.getScore() )) {
					selectedSubTitles = remoteSubTitles;
				}
			}			
		}

		if (selectedSubTitles != null && selectedSubTitles.getScore() >= 6) {

			Path tempFile = Files.createTempFile( "dynamo", "subtitles" );
			Files.write(tempFile, selectedSubTitles.getData(), StandardOpenOption.WRITE);			
			return tempFile;

		}
		
		return null;
	}

}
