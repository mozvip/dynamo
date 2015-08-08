package dynamo.backlog.tasks.music;

import dynamo.core.model.Task;
import dynamo.model.music.MusicAlbum;

public class FindMusicAlbumImageTask extends Task {

	private MusicAlbum album;

	public FindMusicAlbumImageTask( MusicAlbum album ) {
		this.album = album;
	}
	
	public MusicAlbum getAlbum() {
		return album;
	}
	
	public void setAlbum(MusicAlbum album) {
		this.album = album;
	}
	
	@Override
	public String toString() {
		return String.format( "Finding image for album <a href='%s'>%s</a>", album.getRelativeLink(), album.toString());
	}

}
