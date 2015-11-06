package dynamo.providers;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.MovieProvider;
import dynamo.model.movies.MovieManager;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;
import hclient.SimpleResponse;

public class UsenetCrawlerProvider extends DownloadFinder implements MovieProvider {

	private static final String BASE_URL = "https://www.usenet-crawler.com";

	@Configurable(category = "Providers", name="UsenetCrawler Login", disabled="#{!UsenetCrawlerProvider.enabled}", required="#{UsenetCrawlerProvider.enabled}")
	private String login;

	@Configurable(category = "Providers", name="UsenetCrawler Password", disabled="#{!UsenetCrawlerProvider.enabled}", required="#{UsenetCrawlerProvider.enabled}")
	private String password;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality, Language audioLanguage, Language subtitlesLanguage) throws Exception {

		while (!isReady()) {
			try {
				Thread.sleep( 1000 );
			} catch (InterruptedException e) {
			}
		}			
		
		long minSize = MovieManager.getInstance().getMinimumSizeForMovie(videoQuality);
		
		name = plus(name);
		
		String searchURL = String.format("%s/search?val=%s&min=%d&t=2000", BASE_URL, name, minSize);
		if (audioLanguage == Language.EN) {
			searchURL += "&audiolang=0";
		}
		
		WebDocument document = client.getDocument( searchURL, HTTPClient.REFRESH_ONE_DAY );
		Elements rows = document.jsoup("#browsetable tr[id]");
		
		List<SearchResult> results = new ArrayList<>();
		for (Element row : rows) {
			String title = row.select("td.item a").text();
			float size = parseSize( row.select("td.less.right").text() );
			Element downloadLink = row.select("a[title*=Download Nzb]").first();
			if (downloadLink != null) {
				results.add( new SearchResult(this, SearchResultType.NZB, title, downloadLink.absUrl("href"), searchURL, size, false) );
			}
		}

		return results;
	}
	
	@Override
	public void configureProvider() throws Exception {
		WebDocument loginPage = client.getDocument("https://www.usenet-crawler.com/login");
		SimpleResponse result = client.submit(loginPage.jsoupSingle("#content form"), "username=" + login, "password=" + password, "rememberme=on");
		if (result.getLastRedirectLocation() == null || !result.getLastRedirectLocation().toString().equals( "https://www.usenet-crawler.com/")) {
			ErrorManager.getInstance().reportError("Login failed for " + toString() + ", disabling provider");
			setEnabled( false );
		}
	}
	
	@Override
	public String getLabel() {
		return "Usenet Crawler";
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return true;
	}

}
