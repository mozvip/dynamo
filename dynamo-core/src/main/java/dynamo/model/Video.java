package dynamo.model;

import java.nio.file.Path;

public interface Video {
	
	public boolean isSubtitled();
	
	public void setSubtitled( boolean subtitled );
	
	public Path getSubtitlesPath();
	
	public void setSubtitlesPath( Path subtitlesPath);
	
	public boolean isWatched();
	
	public void setWatched( boolean watched );

}
