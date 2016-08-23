package dynamo.suggesters.music;

import java.net.MalformedURLException;

import dynamo.core.configuration.ClassDescription;

@ClassDescription(label="Amazon.com best sellers")
public class AmazonCOMBestSellersAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.com/gp/rss/bestsellers/music" );
	}

}
