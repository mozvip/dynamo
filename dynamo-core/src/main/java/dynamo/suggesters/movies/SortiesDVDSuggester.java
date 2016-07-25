package dynamo.suggesters.movies;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omertron.themoviedbapi.MovieDbException;

import core.WebDocument;
import core.WebResource;
import dynamo.core.Language;
import dynamo.movies.model.MovieManager;
import hclient.HTTPClient;

public class SortiesDVDSuggester implements MovieSuggester {

	private static int MAX_URLS = 10;
	
	private void extract( WebDocument document ) throws MovieDbException, IOException, URISyntaxException, ParseException {
		Elements elements = document.jsoup("div.row");
		if (elements != null) {
			for (Element row : elements) {
				Element imageElement = row.select("img").first();
				if (imageElement == null) {
					continue;
				}
				String imageURL = imageElement.absUrl("src");
				WebResource image = new WebResource( imageURL, document.getOriginalURL());
				Elements links = row.select("header>h2>a");
				if (links != null && !links.isEmpty()) {
					Element firstLink = links.first();
					String title = firstLink.text();
					String suggestionURL = firstLink.absUrl("href");
					MovieManager.getInstance().suggestByName(title, -1, image, Language.FR, false, suggestionURL);
				}
			}
		}
	}

	@Override
	public void suggestMovies() throws Exception {
		for (int i=1; i <= MAX_URLS; i++) {
			String url = String.format("http://www.sortiesdvd.com/page-%d.html", i);
			extract( HTTPClient.getInstance().getDocument( url, HTTPClient.REFRESH_ONE_DAY ));
		}
	}
	
	@Override
	public String toString() {
		return "sortiesdvd.com";
	}

}
