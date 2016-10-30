package dynamo.tvshows.model;

import java.io.Serializable;
import java.nio.file.Path;

public class UnrecognizedFolder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Path path;
	
	public UnrecognizedFolder( Path path ) {
		this.path = path;
	}
	
	public Path getPath() {
		return path;
	}
	
	public void setPath(Path path) {
		this.path = path;
	}

}
