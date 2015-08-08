package dynamo.suggesters.music;

import java.net.MalformedURLException;

public class AmazonFRBestSellersAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.fr/gp/rss/bestsellers/music" );
	}

	@Override
	public String toString() {
		return "Amazon.fr best sellers";
	}
	
}
