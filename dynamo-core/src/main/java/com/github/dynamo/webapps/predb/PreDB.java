package com.github.dynamo.webapps.predb;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.parsers.ParsedMovieInfo;
import com.github.dynamo.parsers.VideoNameParser;
import com.github.dynamo.suggesters.movies.MovieSuggester;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebDocument;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

@ClassDescription(label="PreDB")
public class PreDB implements MovieSuggester {
	
	private int MAX_PAGE_FOR_SUGGESTION = 3;
	
	private PreDBService service = null;
	
	private PreDB() {
		service = new Retrofit.Builder().baseUrl("http://predb.me").build().create(PreDBService.class);
	}

	static class SingletonHolder {
		static PreDB instance = new PreDB();
	}

	public static PreDB getInstance() {
		return SingletonHolder.instance;
	}

	protected WebDocument getWebDocumentFromResponse( ResponseBody response, Charset charset ) throws IOException {
		String contents = StringUtils.toEncodedString(response.bytes(), charset);
		return new WebDocument( null, contents );
	}

	public WebDocument getResultsForCatsTagAndPage(String cats, String tag, int page) throws IOException {
		Response<ResponseBody> execute = service.getResultsForCatsTagAndPage(cats, tag, page).execute();
		Charset charset = Charset.forName("UTF-8");
		return getWebDocumentFromResponse( execute.body(), charset );
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
