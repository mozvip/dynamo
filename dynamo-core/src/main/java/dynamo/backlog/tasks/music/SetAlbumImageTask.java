package dynamo.backlog.tasks.music;

import dynamo.core.model.Task;
import dynamo.model.music.MusicAlbum;

public class SetAlbumImageTask extends Task {
	
	private MusicAlbum musicAlbum;
	private String localImagePath;

	public SetAlbumImageTask(MusicAlbum musicAlbum, String localImagePath) {
		super();
		this.musicAlbum = musicAlbum;
		this.localImagePath = localImagePath;
	}
	
	public String getLocalImagePath() {
		return localImagePath;
	}
	
	public MusicAlbum getMusicAlbum() {
		return musicAlbum;
	}

	@Override
	public String toString() {
		return String.format("Set album image for %s", musicAlbum.toString());
	}

}
