package dynamo.providers;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import core.RegExp;
import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.configuration.ClassDescription;
import dynamo.core.configuration.Configurable;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadLocation;
import dynamo.model.result.SearchResultType;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.suggesters.movies.MovieSuggester;

@ClassDescription(label="RARBG")
public class RARBGProvider extends DownloadFinder implements MovieSuggester {
	
	private final static int MAX_PAGES = 10;
	
	private final WebClient webClient = new WebClient();
	
	@Configurable(category="Providers", name="RARBG base URL", defaultValue="https://rarbg.to")
	private String baseURL = "https://rarbg.to";
	
	public String getBaseURL() {
		return baseURL;
	}
	
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	@Override
	public void suggestMovies() throws Exception {
		
		for( int page=1; page<=MAX_PAGES; page++) {
			String url = String.format("%s/torrents.php?category=movies&page=%d", baseURL, page);
			Page webPage = webClient.getPage( url );
			WebDocument currentPage = new WebDocument(url, webPage.getWebResponse().getContentAsString());
			
			Elements rows = currentPage.jsoup("tr.lista2");
			for (Element element : rows) {
				Element torrentLink = element.child(1).select("a").first();
				
				Element imdbLink = null;
				String imdbId = null;
				Elements imdbLinks = element.select("a[href*=?imdb=]");
				if (imdbLinks != null) {
					imdbLink = imdbLinks.first();
					imdbId = RegExp.extract( imdbLink.attr("href"), ".*imdb=(\\w+)"); 
				}
				
				String title = torrentLink.attr("title");
				String torrentPageURL = torrentLink.absUrl("href");
				String size = element.child(3).text();
				
				if (imdbId != null) {
					Movie suggestion = MovieManager.getInstance().suggestImdbId(imdbId, null, Language.EN, torrentPageURL);
					
					Page torrentPage = webClient.getPage(torrentPageURL);
					WebDocument torrentPageDocument = new WebDocument(url, torrentPage.getWebResponse().getContentAsString());
					
					Element torrentDownloadLink = torrentPageDocument.jsoupSingle("a[href*=/download.php?id=]");
					
					DownloadLocation dl = new DownloadLocation( SearchResultType.TORRENT, torrentDownloadLink.absUrl("href") );
						
					// TODO
					// Collection downloadLocations = new ArrayList<>();
					// Elements relatedRows = currentPage.jsoup("tr.lista2");
					
					
					DownloadableManager.getInstance().saveDownloadLocation(suggestion.getId(), title, "RARBG", this.getClass(), torrentPageURL, parseSize(size), dl);
				} else {
					
				}


			}
			
		}

		
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return false;
	}

	@Override
	public void configureProvider() throws Exception {
		webClient.getOptions().setUseInsecureSSL( true );
		webClient.getOptions().setCssEnabled( false );
	}

}
