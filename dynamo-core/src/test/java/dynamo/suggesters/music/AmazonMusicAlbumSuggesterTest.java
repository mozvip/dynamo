package dynamo.suggesters.music;

import java.net.MalformedURLException;

import org.junit.Test;

public class AmazonMusicAlbumSuggesterTest {

	@Test
	public void testSuggestAlbums() throws MalformedURLException {
		new AmazonFRBestSellersAlbumSuggester().suggestAlbums();
	}

}
