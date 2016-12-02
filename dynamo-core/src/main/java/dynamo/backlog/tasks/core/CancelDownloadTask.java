package dynamo.backlog.tasks.core;

import dynamo.core.model.Task;
import dynamo.model.result.SearchResult;

public class CancelDownloadTask extends Task implements ImmediateTask {

	private SearchResult result;

	public CancelDownloadTask( SearchResult result ) {
		super();
		this.result = result;
	}
	
	public SearchResult getResult() {
		return result;
	}

	@Override
	public String toString() {
		return String.format( "Cancelling download %s", result.getTitle() );
	}

}
