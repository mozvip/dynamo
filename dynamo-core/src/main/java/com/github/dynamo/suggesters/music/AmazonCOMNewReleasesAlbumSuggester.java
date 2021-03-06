package com.github.dynamo.suggesters.music;

import java.net.MalformedURLException;

import com.github.dynamo.core.configuration.ClassDescription;

@ClassDescription(label="Amazon.com new releases")
public class AmazonCOMNewReleasesAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.com/gp/rss/new-releases/music" );
	}
	
}
