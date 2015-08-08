package dynamo.backlog.tasks.core;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioFileFilter implements DirectoryStream.Filter<Path> {

	@Override
	public boolean accept(Path entry) throws IOException {
		
		String[] extensions = new String[] {".mp3", ".m4a", ".wma", ".flac", ".ogg", ".ac3" };
		
		if (Files.isDirectory(entry)) {
			return true;
		}
		
		for (String suffix : extensions) {
			if (entry.getFileName().toString().toLowerCase().endsWith(suffix)) {
				return true;
			}
		}
		
		return false;
	}
	
	private AudioFileFilter() {
	}
	
	private static AudioFileFilter instance = new AudioFileFilter();
	
	public static AudioFileFilter getInstance() {
		return instance;
	}	

}
