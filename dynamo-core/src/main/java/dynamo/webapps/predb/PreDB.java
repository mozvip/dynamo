package dynamo.webapps.predb;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.RegExp;
import core.WebDocument;
import dynamo.core.Language;
import dynamo.core.configuration.ClassDescription;
import dynamo.movies.model.MovieManager;
import dynamo.parsers.ParsedMovieInfo;
import dynamo.parsers.VideoNameParser;
import dynamo.suggesters.movies.MovieSuggester;
import hclient.HTTPClient;
import hclient.RetrofitClient;
import retrofit.RestAdapter;
import retrofit.client.Response;

@ClassDescription(label="PreDB")
public class PreDB implements MovieSuggester {
	
	private int MAX_PAGE_FOR_SUGGESTION = 3;
	
	private PreDBService service = null;
	
	private PreDB() {
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://predb.me").setClient( new RetrofitClient() ).build();
		service = restAdapter.create(PreDBService.class);
	}

	static class SingletonHolder {
		static PreDB instance = new PreDB();
	}

	public static PreDB getInstance() {
		return SingletonHolder.instance;
	}

	protected WebDocument getWebDocumentFromResponse( Response response ) throws IOException {
		byte[] data = new byte[ (int) response.getBody().length() ];
		IOUtils.read( response.getBody().in(), data );	
		return new WebDocument( response.getUrl(), data );
	}

	public WebDocument getResultsForCatsTagAndPage(String cats, String tag, int page) throws IOException {
		return getWebDocumentFromResponse( service.getResultsForCatsTagAndPage(cats, tag, page) );
	}

	@Override
	public void suggestMovies() throws Exception {

		for (int i=1; i<=MAX_PAGE_FOR_SUGGESTION; i++) {
			WebDocument document = getResultsForCatsTagAndPage("movies", null, i);
			Elements posts = document.jsoup(".post");
			for (Element element : posts) {
				
				String id = element.attr("id");
				
				String suggestionURL = String.format("http://predb.me/?post=%s", id);
				WebDocument d = HTTPClient.getInstance().getDocument( suggestionURL, HTTPClient.REFRESH_ONE_WEEK);
				
				Element imdbLink = d.jsoupSingle("a.ext-link[href*=imdb]");
				String imdbId = null;
				if (imdbLink != null) {
					String imdbURL = imdbLink.absUrl("href");
					imdbId = RegExp.extract( imdbURL, "http://www.imdb.com/title/(\\w+).*");
					MovieManager.getInstance().suggestImdbId( imdbId, null, Language.EN, suggestionURL );
				} else {
					String title = element.select("h2 a").text();
					ParsedMovieInfo movieInfo = VideoNameParser.getMovieInfo( title );
					if (movieInfo != null ) {
						MovieManager.getInstance().suggestByName(movieInfo.getName(), movieInfo.getYear(), null, Language.EN, false, suggestionURL);
					}
				}
			}
		}
	}

}
