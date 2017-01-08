package com.github.dynamo.suggesters.music;

import java.net.MalformedURLException;

import com.github.dynamo.core.configuration.ClassDescription;

@ClassDescription(label="Amazon.fr best sellers")
public class AmazonFRBestSellersAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.fr/gp/rss/bestsellers/music" );
	}
	
}
