package dynamo.core;

import org.apache.commons.lang3.StringUtils;

public enum VideoSource {

	DVD( new String[] {"dvdrip", "dvd-rip"}),
	BLURAY( new String[] {"bluray", "blu-ray"}),
	HDTV( new String[] {"hdtv"}),
	WEB_DL( new String[] {"WEB-DL", "WEBDL", "WEB.DL"});

	private String[] identifiers;

	private VideoSource(String[] identifiers) {
		this.identifiers = identifiers;
	}

	public boolean match( String text ) {
		for (String identifier : identifiers) {
			if (StringUtils.containsIgnoreCase(text, identifier)) {
				return true;
			}
		}
		return false;
	}

	public static VideoSource findMatch( String text ) {
		for (VideoSource source : VideoSource.values()) {
			if (source.match( text )) {
				return source;
			}
		}
		return null;
	}

}
