package dynamo.ui;

import java.io.Serializable;

public class Folder implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String path;
	
	public Folder() {
	}
	
	public Folder( String path ) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

}
