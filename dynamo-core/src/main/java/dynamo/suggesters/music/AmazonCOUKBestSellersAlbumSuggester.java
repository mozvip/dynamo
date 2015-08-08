package dynamo.suggesters.music;

import java.net.MalformedURLException;

public class AmazonCOUKBestSellersAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.co.uk/gp/rss/bestsellers/music" );
	}

	@Override
	public String toString() {
		return "Amazon.co.uk best sellers";
	}
	
}
