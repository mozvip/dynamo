package dynamo.backlog.tasks.core;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SubtitlesFileFilter implements DirectoryStream.Filter<Path> {

	@Override
	public boolean accept(Path entry) {
		
		String[] extensions = new String[] { ".srt", ".ass", ".ssa", ".sub", ".idx", ".usf" };
		
		if (Files.isDirectory(entry)) {
			return true;
		}
		
		for (String suffix : extensions) {
			if (entry.getFileName().toString().endsWith(suffix)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static SubtitlesFileFilter instance = new SubtitlesFileFilter();
	
	public static SubtitlesFileFilter getInstance() {
		return instance;
	}

}
