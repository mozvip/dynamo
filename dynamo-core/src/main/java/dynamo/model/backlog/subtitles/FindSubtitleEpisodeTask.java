package dynamo.model.backlog.subtitles;

import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.DownloadableTask;
import model.ManagedEpisode;

public class FindSubtitleEpisodeTask extends DownloadableTask {

	public FindSubtitleEpisodeTask(ManagedEpisode episode) {
		super( episode );
	}

	public ManagedEpisode getEpisode() {
		return (ManagedEpisode)downloadable;
	}
	
	public void setEpisode(ManagedEpisode episode) {
		this.downloadable = episode;
	}
	
	@Override
	public Class<? extends AbstractDynamoQueue> getQueueClass() {
		return FindSubtitlesQueue.class;
	}	

	@Override
	public String toString() {
		return String.format( "Find subtitle for <a href='%s'>%s</a>", getEpisode().getRelativeLink(), getEpisode().toString() );
	}
	
}
