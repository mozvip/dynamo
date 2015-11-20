package dynamo.backlog.tasks.nzb;

import java.nio.file.Path;

import dynamo.backlog.queues.HTTPDownloadQueue;
import dynamo.core.DynamoTask;
import dynamo.core.configuration.ClassDescription;
import dynamo.core.model.Task;
import dynamo.model.Downloadable;
import dynamo.model.result.SearchResult;

@DynamoTask(queueClass=HTTPDownloadQueue.class)
@ClassDescription(label="Download NZB Method")
public class DownloadNZBTask extends Task {

	private Path nzbFilePath;
	private SearchResult searchResult;
	private Downloadable downloadable;
	
	private String nzbURL; 

	public DownloadNZBTask( String nzbURL ) {
		this.nzbURL = nzbURL;
	}

	public DownloadNZBTask(Path nzbFilePath, SearchResult searchResult, Downloadable downloadable) {
		this.nzbFilePath = nzbFilePath;
		this.nzbURL = searchResult.getUrl();
		this.searchResult = searchResult;
		this.downloadable = downloadable;
	}
	
	public Path getNzbFilePath() {
		return nzbFilePath;
	}
	
	public String getNzbURL() {
		return nzbURL;
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}
	
	public Downloadable getDownloadable() {
		return downloadable;
	}

	@Override
	public String toString() {
		return String.format( "Download NZB at %s", nzbURL );
	}

}
