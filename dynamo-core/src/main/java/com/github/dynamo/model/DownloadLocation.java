package com.github.dynamo.model;

import com.github.dynamo.model.result.SearchResultType;

public class DownloadLocation {

	private SearchResultType type;
	private String url;

	public DownloadLocation(SearchResultType type, String url) {
		super();
		this.type = type;
		this.url = url;
	}

	public SearchResultType getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

}
