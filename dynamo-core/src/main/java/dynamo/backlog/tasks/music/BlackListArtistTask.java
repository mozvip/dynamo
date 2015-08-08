package dynamo.backlog.tasks.music;

import dynamo.core.model.Task;

public class BlackListArtistTask extends Task {
	
	private String artistName;
	
	public BlackListArtistTask( String artistName ) {
		this.artistName = artistName;
	}
	
	public String getArtistName() {
		return artistName;
	}
	
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	
	@Override
	public String toString() {
		return String.format("Black-listing music artist : %s", artistName); 
	}

}
