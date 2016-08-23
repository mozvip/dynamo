package dynamo.suggesters.music;

import java.net.MalformedURLException;

import dynamo.core.configuration.ClassDescription;

@ClassDescription(label="Amazon.fr new releases")
public class AmazonFRNewReleasesAlbumSuggester extends AbstractAmazonRSSMusicSuggester {

	@Override
	public void suggestAlbums() throws MalformedURLException {
		suggest( "http://www.amazon.fr/gp/rss/new-releases/music" );
	}
	
}
