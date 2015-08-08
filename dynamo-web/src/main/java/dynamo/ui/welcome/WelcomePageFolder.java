package dynamo.ui.welcome;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WelcomePageFolder {
	
	private Path path;
	private long freeSpace;
	private boolean available = false;
	
	public WelcomePageFolder( Path path ) throws IOException {
		this.path = path;
		if (Files.isReadable(path)) {
			freeSpace = Files.getFileStore( path ).getUsableSpace();
			available = true;
		}
	}
	
	public boolean isAvailable() {
		return available;
	}
	
	public Path getPath() {
		return path;
	}

	public long getFreeSpace() {
		return freeSpace;
	}
}
