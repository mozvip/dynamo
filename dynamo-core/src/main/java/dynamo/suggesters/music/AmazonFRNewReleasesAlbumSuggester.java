package dynamo.suggesters.music;

import java.net.MalformedURLException;

public class AmazonFRNewReleasesAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.fr/gp/rss/new-releases/music" );
	}

	@Override
	public String toString() {
		return "Amazon.fr new releases";
	}
	
}
