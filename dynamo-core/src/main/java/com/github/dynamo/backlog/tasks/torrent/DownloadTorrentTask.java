package com.github.dynamo.backlog.tasks.torrent;

import java.nio.file.Path;

import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.model.Task;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.result.SearchResult;
import com.github.mozvip.hclient.core.WebResource;

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
		return String.format( "Download torrent from <a href='%s'>URL</a> for %s", url.getUrl(), downloadable.getName() );
	}

}
