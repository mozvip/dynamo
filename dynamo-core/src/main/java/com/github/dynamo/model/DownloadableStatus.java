package com.github.dynamo.model;

import org.apache.commons.lang3.StringUtils;

public enum DownloadableStatus {
	
	FUTURE, DOWNLOADED, SNATCHED, WANTED, IGNORED, ARCHIVED, SUBTITLED, SUGGESTED;
	
	public String getLabel() {
		return StringUtils.capitalize( name().toLowerCase() );
	}

}
