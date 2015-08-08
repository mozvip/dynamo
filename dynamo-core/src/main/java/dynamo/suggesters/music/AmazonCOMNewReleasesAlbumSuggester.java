package dynamo.suggesters.music;

import java.net.MalformedURLException;

public class AmazonCOMNewReleasesAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.com/gp/rss/new-releases/music" );
	}

	@Override
	public String toString() {
		return "Amazon.com new releases";
	}
	
}
