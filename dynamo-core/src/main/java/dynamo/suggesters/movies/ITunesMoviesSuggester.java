package dynamo.suggesters.movies;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import core.WebResource;
import dynamo.core.Language;
import dynamo.model.movies.MovieManager;
import hclient.HTTPClient;

public class ITunesMoviesSuggester implements MovieSuggester {

	@Override
	public void suggestMovies() throws Exception {
		
		String url = "http://www.apple.com/itunes/charts/movies/";
		//http://www.apple.com/fr/itunes/charts/albums/ for France : make this configurable ?

		WebDocument document = HTTPClient.getInstance().getDocument(url, HTTPClient.REFRESH_ONE_DAY);
		
		Elements elements = document.jsoup(".main .section-content li");
		for (Element element : elements) {
			Element img = element.select("img").first();
			String moviesName = img.attr("alt");
			WebResource image = new WebResource(img.absUrl("src"), url);
			MovieManager.getInstance().suggestByName(moviesName, -1, image, Language.EN);
		}
	}
	
	@Override
	public String toString() {
		return "iTunes Chart";
	}
}
