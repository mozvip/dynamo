package dynamo.providers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.configuration.ClassDescription;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.MovieProvider;
import dynamo.finders.music.MusicAlbumFinder;
import dynamo.finders.music.MusicAlbumSearchException;
import dynamo.magazines.MagazineProvider;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import dynamo.movies.model.MovieManager;
import hclient.HTTPClient;
import hclient.SimpleResponse;

@ClassDescription(label="Usenet Crawler")
public class UsenetCrawlerProvider extends DownloadFinder implements MovieProvider, MusicAlbumFinder, MagazineProvider {

	private static final String BASE_URL = "https://www.usenet-crawler.com";

	@Configurable(ifExpression = "UsenetCrawlerProvider.enabled")
	private String login;

	@Configurable(ifExpression = "UsenetCrawlerProvider.enabled")
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

		long minSize = MovieManager.getInstance().getMinimumSizeForMovie(videoQuality);
		
		name = plus(name);
		
		String searchURL = String.format("%s/search?val=%s&min=%d&t=2000", BASE_URL, name, minSize);
		
		return extractResults(searchURL);
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
	public boolean needsLanguageInSearchString() {
		return true;
	}

	@Override
	public List<SearchResult> findMusicAlbum(String artist, String album, MusicQuality quality)
			throws MusicAlbumSearchException {

		int t = quality == MusicQuality.COMPRESSED ? 3010 : 3040;
		int maxSize = quality == MusicQuality.COMPRESSED ? 500000000 : 2000000000;

		try {
			String searchURL = String.format("%s/search?val=%s %s&age=-1&max=%d&index=3&t=%d", BASE_URL, plus(artist), plus(album), maxSize, t);
			return extractResults(searchURL);
		} catch (IOException e) {
			throw new MusicAlbumSearchException( e );
		}
	}

	private List<SearchResult> extractResults(String searchURL) throws IOException {
		WebDocument document = client.getDocument( searchURL, HTTPClient.REFRESH_ONE_DAY );
		Elements rows = document.jsoup("#browsetable tr[id]");
		
		List<SearchResult> results = new ArrayList<>();
		for (Element row : rows) {
			String title = row.select("td.item a").text();
			float size = parseSize( row.select("td.less.right").text() );
			Element downloadLink = row.select("a[title*=Download Nzb]").first();
			if (downloadLink != null) {
				results.add( new SearchResult(this, SearchResultType.NZB, title, downloadLink.absUrl("href"), searchURL, size) );
			}
		}
		return results;
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {
		String searchURL = String.format("%s/search?val=%s&age=-1&index=3&t=7010", BASE_URL, issueSearchString);
		return extractResults( searchURL );
	}

}
