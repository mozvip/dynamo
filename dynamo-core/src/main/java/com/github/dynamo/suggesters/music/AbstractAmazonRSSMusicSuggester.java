package com.github.dynamo.suggesters.music;

import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.suggesters.AmazonRSSSuggester;

public abstract class AbstractAmazonRSSMusicSuggester extends AmazonRSSSuggester implements MusicAlbumSuggester {
	
	@Override
	protected void createSuggestion(String title, String artistName, String imageURL, String rssURL, String suggestionURL ) throws Exception {
		artistName = MusicManager.getArtistName( artistName );
		if (artistName.equals(MusicManager.VARIOUS_ARTISTS) && MusicManager.isOriginalSountrack( title )) {
			artistName = MusicManager.ORIGINAL_SOUNDTRACK;
		}
		title = MusicManager.getAlbumName( title );

		MusicManager.getInstance().suggest(artistName, title, imageURL, rssURL, suggestionURL);
	}

}
