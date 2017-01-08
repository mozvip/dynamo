package com.github.dynamo.model;

public class SuggestionURL {

	private long downloadableId;
	private String url;

	public SuggestionURL(long downloadableId, String url) {
		super();
		this.downloadableId = downloadableId;
		this.url = url;
	}

	public long getDownloadableId() {
		return downloadableId;
	}

	public void setDownloadableId(long downloadableId) {
		this.downloadableId = downloadableId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
