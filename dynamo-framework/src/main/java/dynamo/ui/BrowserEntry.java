package dynamo.ui;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class BrowserEntry implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String label;
	private Path path;

	public BrowserEntry(String label, Path path) {
		super();
		this.label = label;
		this.path = path;
	}

	public BrowserEntry(Path p) {
		this( p == null ? "" : (p.getFileName() != null ? p.getFileName().toString() : p.toAbsolutePath().toString()), p );
	}
	
	public boolean isFolder() {
		return path == null || Files.isDirectory( path );
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getPathString() {
		
		String pathString = path.toAbsolutePath().toString();
		return pathString.replace("\\", "\\\\");
		
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

}
