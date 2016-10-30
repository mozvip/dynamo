package dynamo.tvshows.model;

import java.io.Serializable;
import java.nio.file.Path;

public class UnrecognizedFile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private Path path;
	private String seriesId;

	public UnrecognizedFile(long id, Path path, String seriesId) {
		this.id = id;
		this.path = path;
		this.seriesId = seriesId;
	}

	public long getId() {
		return id;
	}

	public Path getPath() {
		return path;
	}
	
	public String getSeriesId() {
		return seriesId;
	}
	
	@Override
	public boolean equals(Object other) {
		return id == ((UnrecognizedFile)other).getId();
	}
	
	@Override
	public int hashCode() {
		return Long.toString( id ).hashCode();
	}
	

}
