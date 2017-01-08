package com.github.dynamo.providers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.finders.core.MovieProvider;
import com.github.dynamo.finders.music.MusicAlbumFinder;
import com.github.dynamo.finders.music.MusicAlbumSearchException;
import com.github.dynamo.magazines.MagazineProvider;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultType;
import com.github.dynamo.movies.model.MovieManager;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.SimpleResponse;
import com.github.mozvip.hclient.core.WebDocument;

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
		Element loginForm = loginPage.jsoupSingle("#content form");
		if (loginForm != null) {
			SimpleResponse result = client.submit(loginForm, "username=" + login, "password=" + password, "rememberme=on");
			if (result.getLastRedirectLocation() == null || !result.getLastRedirectLocation().toString().equals( "https://www.usenet-crawler.com/")) {
				ErrorManager.getInstance().reportError("Login failed for " + DynamoObjectFactory.getClassDescription( this.getClass() ) + ", disabling provider");
				setEnabled( false );
			}
		} else {
			ErrorManager.getInstance().reportError("Login page is not responsing for " + DynamoObjectFactory.getClassDescription( this.getClass() ) + ", disabling provider");
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
			float size = parseSize( row.select("td.less.right").first().childNode(0).toString() );
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
