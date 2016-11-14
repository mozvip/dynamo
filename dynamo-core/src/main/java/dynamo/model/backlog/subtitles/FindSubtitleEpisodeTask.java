package dynamo.model.backlog.subtitles;

import dynamo.core.model.DownloadableTask;
import dynamo.tvshows.model.ManagedEpisode;

public class FindSubtitleEpisodeTask extends DownloadableTask {

	public FindSubtitleEpisodeTask(ManagedEpisode episode) {
		super( episode );
	}

	public ManagedEpisode getEpisode() {
		return (ManagedEpisode)downloadable;
	}

	@Override
	public String toString() {
		return String.format( "Find subtitle for <a href='%s'>%s</a>", getEpisode().getRelativeLink(), getEpisode().toString() );
	}
	
}
