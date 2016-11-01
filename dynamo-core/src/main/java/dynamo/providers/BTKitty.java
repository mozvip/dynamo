package dynamo.providers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.configuration.ClassDescription;
import dynamo.core.configuration.Configurable;
import dynamo.finders.core.EpisodeFinder;
import dynamo.finders.core.MovieProvider;
import dynamo.magazines.MagazineProvider;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import hclient.SimpleResponse;

@ClassDescription(label="BTKitty")
public class BTKitty extends DownloadFinder implements MagazineProvider, EpisodeFinder, MovieProvider {
	
	@Configurable(ifExpression="BTKitty.enabled", required=true, defaultValue="http://btkitty.bid")
	private String baseURL = "http://btkitty.bid";
	
	public String getBaseURL() {
		return baseURL;
	}
	
	public void setBaseURL(String rootURL) {
		this.baseURL = rootURL;
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {
		return searchResults( issueSearchString );
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return true;
	}

	@Override
	public void configureProvider() throws Exception {
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception {
		return searchResults( String.format("%s S%02dE%02d", seriesName, seasonNumber, episodeNumber) );
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int absoluteEpisodeNumber)
			throws Exception {
		return searchResults( String.format("%s %d", seriesName, absoluteEpisodeNumber) );
	}

	private List<SearchResult> searchResults(String searchString) throws IOException {
		Map<String, Object> params = new HashMap<>();
		params.put("keyword", searchString);
		params.put("hidden", true);
		SimpleResponse response = client.post( baseURL, baseURL, params);
		
		List<SearchResult> results = new ArrayList<>();

		String lastRedirectLocationURL = response.getLastRedirectLocationURL();
		String url = lastRedirectLocationURL.replace("/0/0.html", "/4/0.html");
		
		WebDocument resultsDocument = client.getDocument(url, lastRedirectLocationURL, 0);

		Elements rows = resultsDocument.evaluateJSoup("dl.list-con");
		for (Element element : rows) {
			Element link = element.select("dt a").first();
			String title = link.text();
			
			float sizeInMegs = parseSize( element.select("span.size").text() );

			String magnetLink = element.select("a[href*=magnet:]").first().attr("href");
			
			SearchResult result = new SearchResult(this, SearchResultType.TORRENT, title, magnetLink, url, sizeInMegs);	
			results.add( result );
		}

		return results;
	}

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality, Language audioLanguage,
			Language subtitlesLanguage) throws Exception {
		return searchResults( String.format("%s %d", name, year) );
	}

}
