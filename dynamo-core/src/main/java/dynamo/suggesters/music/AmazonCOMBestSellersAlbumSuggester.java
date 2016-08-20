package dynamo.suggesters.music;

import java.net.MalformedURLException;

public class AmazonCOMBestSellersAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.com/gp/rss/bestsellers/music" );
	}
	
	@Override
	public String getLabel() {
		return "Amazon.com best sellers";
	}

}
