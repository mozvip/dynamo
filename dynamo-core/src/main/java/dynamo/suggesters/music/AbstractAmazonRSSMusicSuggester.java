package dynamo.suggesters.music;

import dynamo.manager.MusicManager;
import dynamo.suggesters.AmazonRSSSuggester;

public abstract class AbstractAmazonRSSMusicSuggester extends AmazonRSSSuggester implements MusicAlbumSuggester {
	
	@Override
	protected void createSuggestion(String title, String artistName, String imageURL, String rssURL) throws Exception {
		artistName = MusicManager.getArtistName( artistName );
		if (artistName.equals(MusicManager.VARIOUS_ARTISTS) && MusicManager.isOriginalSountrack( title )) {
			artistName = MusicManager.ORIGINAL_SOUNDTRACK;
		}
		title = MusicManager.getAlbumName( title );
		MusicManager.getInstance().suggest(artistName, title, null, imageURL, rssURL);
	}

}
