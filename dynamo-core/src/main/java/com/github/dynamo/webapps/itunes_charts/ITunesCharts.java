package com.github.dynamo.webapps.itunes_charts;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.suggesters.movies.MovieSuggester;
import com.github.dynamo.suggesters.music.MusicAlbumSuggester;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebDocument;
import com.github.mozvip.hclient.core.WebResource;

@ClassDescription(label="iTunes Chart")
public class ITunesCharts implements MusicAlbumSuggester, MovieSuggester {
	
	public String getBaseURL() {
		//http://www.apple.com/fr/itunes/charts/albums/ for France : make this configurable ?
		return "http://www.apple.com/itunes/charts";
	}

	@Override
	public void suggestAlbums() throws Exception {
		
		String referer = getBaseURL() + "/albums/";

		WebDocument document = HTTPClient.getInstance().getDocument(referer, HTTPClient.REFRESH_ONE_DAY);
		
		Elements elements = document.jsoup(".main .section-content li");
		for (Element element : elements) {
			Element img = element.select("img").first();
			String albumName = img.attr("alt");
			String artistName = element.select("h4").first().text();
			
			String suggestionURL = img.parent().absUrl("href");
			
			WebDocument albumPage = HTTPClient.getInstance().getDocument( suggestionURL, HTTPClient.REFRESH_ONE_WEEK );
			String imageURL = albumPage.jsoupSingle("img[src-swap-high-dpi]").absUrl("src-swap-high-dpi");
			imageURL = imageURL.replace("200x200bb", "340x340bb");
			// FIXME : extract genre

			MusicManager.getInstance().suggest(artistName, albumName, imageURL, suggestionURL, suggestionURL);
		}
	}

	@Override
	public void suggestMovies() throws Exception {
		String referer = getBaseURL() + "/movies/";

		WebDocument document = HTTPClient.getInstance().getDocument(referer, HTTPClient.REFRESH_ONE_DAY);
		
		Elements elements = document.jsoup(".main .section-content li");
		for (Element element : elements) {
			Element img = element.select("img").first();
			String movieName = img.attr("alt");
			String genre = element.select("h4").first().text();
			
			String suggestionURL = img.parent().absUrl("href");
			
			WebDocument moviePage = HTTPClient.getInstance().getDocument( suggestionURL, HTTPClient.REFRESH_ONE_WEEK );
			String imageURL = moviePage.jsoupSingle("img[src-swap-high-dpi]").absUrl("src-swap-high-dpi");
			// imageURL = imageURL.replace("200x200bb", "340x340bb");

			// TODO : use genre ?
			
			int year = -1;
			
			String[] groups = RegExp.parseGroups( movieName, "(.*)\\((\\d{4})\\)" );
			if ( groups != null ) {
				movieName = groups[0].trim();
				year = Integer.parseInt( groups[1]);
			}

			MovieManager.getInstance().suggestByName(movieName, year, new WebResource(imageURL, referer), Language.EN, false, suggestionURL);
		}
	}
	

}
