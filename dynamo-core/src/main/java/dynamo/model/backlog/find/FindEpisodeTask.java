package dynamo.model.backlog.find;

import dynamo.model.backlog.core.FindDownloadableTask;
import model.ManagedEpisode;

public class FindEpisodeTask extends FindDownloadableTask<ManagedEpisode> {

	public FindEpisodeTask( ManagedEpisode episode ) {
		super( episode );
	}
	
	public ManagedEpisode getEpisode() {
		return (ManagedEpisode)downloadable;
	}	

	@Override
	public String toString() {
		ManagedEpisode episode = getDownloadable();
		return String.format( "Find download for <a href='%s'>%s</a>", episode.getRelativeLink(), episode.toString() );
	}

}
