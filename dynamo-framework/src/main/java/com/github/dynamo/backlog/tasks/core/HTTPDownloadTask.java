package com.github.dynamo.backlog.tasks.core;

import java.nio.file.Path;

import com.github.dynamo.core.model.Task;

public class HTTPDownloadTask extends Task {
	
	private String url;
	private String referer;
	private Path destinationFile;
	private boolean image = false;

	public HTTPDownloadTask(String url, String referer, boolean image, Path destinationFile ) {
		this.url = url;
		this.referer = referer;
		this.image = image;
		this.destinationFile = destinationFile;
	}

	public String getUrl() {
		return url;
	}
	
	public String getReferer() {
		return referer;
	}

	public Path getDestinationFile() {
		return destinationFile;
	}
	
	public boolean isImage() {
		return image;
	}

	@Override
	public String toString() {
		return String.format( "Downloading <a href='%s'>%s</a> to %s", url, url, destinationFile.toAbsolutePath().toString() );
	}

}
