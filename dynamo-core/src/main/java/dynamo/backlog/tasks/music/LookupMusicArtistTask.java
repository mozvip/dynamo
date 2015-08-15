package dynamo.backlog.tasks.music;

import dynamo.backlog.queues.AllMusicQueue;
import dynamo.core.DynamoTask;
import dynamo.core.model.Task;
import dynamo.model.music.MusicArtist;

@DynamoTask(queueClass=AllMusicQueue.class)
public class LookupMusicArtistTask extends Task {

	private MusicArtist artist;
	
	public LookupMusicArtistTask( MusicArtist artist ) {
		this.artist = artist;
	}
	
	public MusicArtist getArtist() {
		return artist;
	}
	
	public void setArtist(MusicArtist artist) {
		this.artist = artist;
	}
	
	@Override
	public String toString() {
		return String.format("Look up artist metadata for %s", artist.getName());
	}

}
