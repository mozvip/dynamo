package com.github.dynamo.core.model;

import com.github.dynamo.model.DownloadableStatus;

public class DownloableCount {

	private String type;
	private DownloadableStatus status;
	private int count;

	public DownloableCount(String type, DownloadableStatus status, int count) {
		super();
		this.type = type;
		this.status = status;
		this.count = count;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DownloadableStatus getStatus() {
		return status;
	}

	public void setStatus(DownloadableStatus status) {
		this.status = status;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
