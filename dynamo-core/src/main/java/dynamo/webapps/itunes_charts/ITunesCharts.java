package dynamo.webapps.itunes_charts;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.RegExp;
import core.WebDocument;
import core.WebResource;
import dynamo.core.Language;
import dynamo.manager.MusicManager;
import dynamo.model.movies.MovieManager;
import dynamo.suggesters.movies.MovieSuggester;
import dynamo.suggesters.music.MusicAlbumSuggester;
import hclient.HTTPClient;

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
			String imageURL = img.absUrl("src");
			
			// FIXME : extract genre

			MusicManager.getInstance().suggest(artistName, albumName, null, imageURL, referer);
		}
	}
	
	@Override
	public String toString() {
		return "iTunes Chart";
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
			String imageURL = img.absUrl("src");
			
			// TODO : use genre ?
			
			int year = -1;
			
			String[] groups = RegExp.parseGroups( movieName, "(.*)\\((\\d{4})\\)" );
			if ( groups != null ) {
				movieName = groups[0].trim();
				year = Integer.parseInt( groups[1]);
			}

			MovieManager.getInstance().suggestByName(movieName, year, new WebResource(imageURL, referer), Language.EN, false);
		}
	}
	

}
