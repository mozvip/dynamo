package com.github.dynamo.suggesters.music;

import java.net.MalformedURLException;

import com.github.dynamo.core.configuration.ClassDescription;

@ClassDescription(label="Amazon.co.uk best sellers")
public class AmazonCOUKBestSellersAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.co.uk/gp/rss/bestsellers/music" );
	}
	
}
