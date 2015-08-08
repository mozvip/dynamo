package dynamo.suggesters.music;

import java.net.MalformedURLException;

public class AmazonCOUKNewReleasesAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.co.uk/gp/rss/new-releases/music" );
	}

	@Override
	public String toString() {
		return "Amazon.co.uk new releases";
	}
	
}
