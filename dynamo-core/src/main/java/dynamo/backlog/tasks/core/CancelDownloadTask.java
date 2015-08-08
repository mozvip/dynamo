package dynamo.backlog.tasks.core;

import dynamo.core.model.Task;
import dynamo.model.Downloadable;
import dynamo.model.result.SearchResult;

public class CancelDownloadTask extends Task {
	
	private Downloadable downloadable;
	private SearchResult result;

	public CancelDownloadTask( Downloadable downloadable, SearchResult result ) {
		super();
		this.downloadable = downloadable;
		this.result = result;
	}

	public Downloadable getDownloadable() {
		return downloadable;
	}
	
	public SearchResult getResult() {
		return result;
	}

	@Override
	public String toString() {
		return String.format( "Cancelling download for %s", downloadable.toString() );
	}

}
