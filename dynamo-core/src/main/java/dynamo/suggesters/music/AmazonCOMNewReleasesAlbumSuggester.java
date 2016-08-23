package dynamo.suggesters.music;

import java.net.MalformedURLException;

import dynamo.core.configuration.ClassDescription;

@ClassDescription(label="Amazon.com new releases")
public class AmazonCOMNewReleasesAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.com/gp/rss/new-releases/music" );
	}
	
}
