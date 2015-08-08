package dynamo.backlog.tasks.movies;

import dynamo.core.configuration.Configurable;
import dynamo.core.model.DaemonTask;
import dynamo.model.movies.MovieManager;

public class MovieCleanupTask extends DaemonTask {
	
	@Configurable(category="Movies", name="Rename movie files", defaultValue="false", disabled="#{!MovieManager.enabled}", required="#{MovieManager.enabled}")
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
