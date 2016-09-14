package dynamo.backlog.tasks.torrent;

import java.nio.file.Path;

import core.WebResource;
import dynamo.backlog.queues.HTTPDownloadQueue;
import dynamo.core.DynamoTask;
import dynamo.core.configuration.ClassDescription;
import dynamo.core.model.Task;
import dynamo.model.Downloadable;
import dynamo.model.result.SearchResult;

@DynamoTask(queueClass=HTTPDownloadQueue.class)
@ClassDescription(label="Download Torrents Method")
public class DownloadTorrentTask extends Task {
	
	private Path torrentFilePath;
	private SearchResult searchResult;
	private Downloadable downloadable;
	
	private WebResource url; 

	public DownloadTorrentTask( WebResource url ) {
		this.url = url;
	}

	public DownloadTorrentTask(Path torrentFilePath, SearchResult searchResult, Downloadable downloadable) {
		this.torrentFilePath = torrentFilePath;
		this.url = new WebResource( searchResult.getUrl(), searchResult.getReferer() );
		this.searchResult = searchResult;
		this.downloadable = downloadable;
	}
	
	public DownloadTorrentTask(String url, SearchResult searchResult, Downloadable downloadable) {
		this.url = new WebResource( searchResult.getUrl(), searchResult.getReferer() );
		this.searchResult = searchResult;
		this.downloadable = downloadable;
	}

	public WebResource getURL() {
		return url;
	}
	
	public Path getTorrentFilePath() {
		return torrentFilePath;
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}
	
	public Downloadable getDownloadable() {
		return downloadable;
	}

	@Override
	public String toString() {
		return String.format( "Download torrent from <a href='%s'>URL</a>", url.getUrl() );
	}

}
