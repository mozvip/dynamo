package com.github.dynamo.suggesters.music;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.model.music.MusicArtist;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.WebDocument;

@ClassDescription(label="AllMusic.com New Releases")
public class AllMusicNewReleasesSuggester implements MusicAlbumSuggester {
	
	@Override
	public void suggestAlbums() {

		String url = "http://www.allmusic.com/newreleases";

		try {
			WebDocument document = HTTPClient.getInstance().getDocument(url, HTTPClient.REFRESH_ONE_DAY);

			Elements albumElements = document.jsoup("div.featured");
			for (Element albumElement : albumElements) {
				
				String artistName = "Various Artists";
				Elements artistLinks = albumElement.select(".artist > a");
				if (artistLinks.size() > 0) {
					artistName = artistLinks.get(0).text();
				}
				Element album = albumElement.select(".title > a").get(0);

				Element thumbnail = albumElement.select(".album-cover img").get(0);
				
				String albumName = album.ownText();
				
				artistName = MusicManager.getArtistName(artistName);
				MusicArtist artist = MusicManager.getInstance().getOrCreateArtist( artistName );

				MusicAlbum mAlbum = MusicManager.getInstance().getAlbum( artistName, albumName );
				if (mAlbum == null) {
					

					String genre = "Unknown";
					Elements styles = albumElement.select(".styles > a");
					if ( styles != null && styles.size() > 0 ) {
						genre = styles.get(0).ownText();
					}
					
					String imageURL = thumbnail.attr("abs:data-original");
					try {
						MusicManager.getInstance().suggest(artist.getName(), albumName, imageURL, url, url);
					} catch (ExecutionException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			}

		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
	}

}
