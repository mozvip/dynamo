package dynamo.backlog.tasks.music;

import dynamo.core.model.Task;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicFile;

public class DeleteMusicFileTask extends Task {
	
	private MusicFile musicFile;
	private MusicAlbum musicAlbum;
	
	public DeleteMusicFileTask(MusicFile musicFile) {
		this.musicFile = musicFile;
	}
	
	public DeleteMusicFileTask(MusicAlbum musicAlbum, MusicFile musicFile) {
		this.musicAlbum = musicAlbum;
		this.musicFile = musicFile;
	}

	public MusicFile getMusicFile() {
		return musicFile;
	}
	
	public MusicAlbum getMusicAlbum() {
		return musicAlbum;
	}

	@Override
	public String toString() {
		return String.format("Deleting music file : %s", musicFile.getPath().toString());
	}

}
