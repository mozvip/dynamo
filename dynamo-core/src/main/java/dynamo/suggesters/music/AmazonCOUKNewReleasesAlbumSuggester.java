package dynamo.suggesters.music;

import java.net.MalformedURLException;

import dynamo.core.configuration.ClassDescription;

@ClassDescription(label="Amazon.co.uk new releases")
public class AmazonCOUKNewReleasesAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.co.uk/gp/rss/new-releases/music" );
	}
	
}
