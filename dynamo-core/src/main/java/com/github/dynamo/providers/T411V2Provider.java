package com.github.dynamo.providers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.finders.core.EpisodeFinder;
import com.github.dynamo.finders.core.MovieProvider;
import com.github.dynamo.finders.core.TVShowSeasonProvider;
import com.github.dynamo.finders.music.MusicAlbumFinder;
import com.github.dynamo.finders.music.MusicAlbumSearchException;
import com.github.dynamo.magazines.MagazineProvider;
import com.github.dynamo.model.ebooks.books.Book;
import com.github.dynamo.model.ebooks.books.BookFinder;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultType;
import com.github.mozvip.hclient.SimpleResponse;
import com.github.mozvip.hclient.core.WebDocument;

@ClassDescription(label="Torrent 411")
public class T411V2Provider extends DownloadFinder
		implements EpisodeFinder, MusicAlbumFinder, TVShowSeasonProvider, MovieProvider, BookFinder, MagazineProvider {

	@Configurable(ifExpression = "T411V2Provider.enabled", defaultValue = "https://t411.si")
	private String baseURL = "https://t411.si/";

	@Configurable(ifExpression = "T411V2Provider.enabled")
	private String username;

	@Configurable(ifExpression = "T411V2Provider.enabled")
	private String password;

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return false;
	}

	@Override
	public void configureProvider() throws Exception {
		SimpleResponse response = client.post(String.format("%s/login/checklogin.php", baseURL), baseURL,
				"username=" + username, "password=" + password);
		List<Cookie> cookies = client.getCookies(new URL(response.getUrl()).getAuthority());
		for (Cookie cookie : cookies) {
			System.out.println(cookie.getName());
		}
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {
		List<SearchResult> results = extract(issueSearchString, 5, 508);
		return results;
	}
	
	private List<SearchResult> extract(String search, int category, int subcategory) throws UnsupportedEncodingException, IOException {
		
		String url;
		if (subcategory > 0) {
			url = String.format("%s/torrents/search/?search=%s&category=%d&subcategory=%d&submit=", baseURL, search, category, subcategory);
		} else {
			url = String.format("%s/torrents/search/?search=%s&category=%d&subcategory=&submit=", baseURL, search, category);
		}
		
		WebDocument document = client.get(url).getDocument();
		
		Elements rows = document.jsoup("tr.isItem.isItemDesk");
		List<SearchResult> results = new ArrayList<>();
		for (Element element : rows) {
			String title = element.select(".m-name").text();
			String sizeStr = element.select(".m-taille").text();
			
			Element link = element.select(".m-name a").first();
			String torrentPageURL = link.absUrl("href");
			
			SimpleResponse torrentPageResponse = client.get(torrentPageURL);
			WebDocument torrentPageDocument = torrentPageResponse.getDocument();
			
			Elements downloadLinks = torrentPageDocument.jsoup(".trTorrentDL a.aic");
			for (Element downloadLink : downloadLinks) {
				String downloadURL = downloadLink.attr("href");
				if (!downloadURL.startsWith("magnet")) {
					downloadURL = downloadLink.absUrl("href");
				}
				SearchResult result = new SearchResult(this, SearchResultType.TORRENT, title, downloadURL, torrentPageURL, parseSize(sizeStr));
				results.add( result );
			}
		}
		return results;
	}

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality, Language audioLanguage,
			Language subtitlesLanguage) throws Exception {
		String search = name;
		if (year > 1000) {
			search = String.format("%s %d", name, year);
		}
		return extract(search, 1, -1);
	}

	@Override
	public List<SearchResult> findDownloadsForSeason(String aka, Language audioLanguage, int seasonNumber)
			throws Exception {
		String search1 = String.format("%s S%02d", aka, seasonNumber);
		String search2 = String.format("%s Saison %d", aka, seasonNumber);

		List<SearchResult> results = extract(search1, 2, -1);
		results.addAll( extract(search2, 2, -1) );
		return results;
	}

	@Override
	public List<SearchResult> findMusicAlbum(String artist, String album, MusicQuality quality)
			throws MusicAlbumSearchException {

		String search = String.format("%s %s", artist, album);
		
		int subcategory = quality == MusicQuality.LOSSLESS ? 401 : 402;

		try {
			return extract(search, 4, subcategory);
		} catch (IOException e) {
			throw new MusicAlbumSearchException( e );
		}
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int seasonNumber,
			int episodeNumber) throws Exception {
		String search1 = String.format("%s S%02dE%02d", seriesName, seasonNumber, episodeNumber);

		List<SearchResult> results = extract(search1, 2, -1);
		return results;
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int absoluteEpisodeNumber)
			throws Exception {
		String search1 = String.format("%s %d", seriesName, absoluteEpisodeNumber);
		List<SearchResult> results = extract(search1, 2, -1);
		return results;
	}

	@Override
	public List<SearchResult> findBook(Book book) throws Exception {
		int subcategory = 501;
		if (book.getLanguage() == Language.EN) {
			subcategory = 502;
		}
		return extract(book.getName(), 5, subcategory);
	}

}
