package dynamo.backlog.tasks.music;

import java.nio.file.Path;

import dynamo.core.model.Task;
import dynamo.model.music.MusicAlbum;

public class ImportMusicFileTask extends Task {

	private Path path;
	private boolean keepSourceFile;
	private MusicAlbum musicAlbum;
	
	public ImportMusicFileTask( MusicAlbum musicAlbum, Path path, boolean keepSourceFile ) {
		this.musicAlbum = musicAlbum;
		this.path = path;
		this.keepSourceFile = keepSourceFile;
	}

	public Path getPath() {
		return path;
	}
	
	public MusicAlbum getMusicAlbum() {
		return musicAlbum;
	}
	
	public boolean isKeepSourceFile() {
		return keepSourceFile;
	}
	
	@Override
	public String toString() {
		return String.format("Importing music file : %s", path.getFileName().toString());
	}

}
