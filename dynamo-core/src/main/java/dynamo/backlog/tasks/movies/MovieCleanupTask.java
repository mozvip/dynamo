package dynamo.backlog.tasks.movies;

import dynamo.core.configuration.Configurable;
import dynamo.core.model.DaemonTask;
import dynamo.movies.model.MovieManager;

public class MovieCleanupTask extends DaemonTask {
	
	@Configurable(defaultValue="false")
	private boolean rename = false;
	
	@Override
	public boolean isEnabled() {
		return MovieManager.getInstance().isEnabled();
	}
	
	public boolean isRename() {
		return rename;
	}
	
	public void setRename(boolean rename) {
		this.rename = rename;
	}
	
	public MovieCleanupTask() {
		super( 24 * 60 );
	}

	@Override
	public String toString() {
		return rename ? "Clean up movie collection & rename files" : "Clean up movie collection";
	}

}
