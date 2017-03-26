package com.github.dynamo.providers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.finders.music.MusicAlbumFinder;
import com.github.dynamo.finders.music.MusicAlbumSearchException;
import com.github.dynamo.games.GameFinder;
import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.magazines.MagazineProvider;
import com.github.dynamo.model.ebooks.books.Book;
import com.github.dynamo.model.ebooks.books.BookFinder;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultType;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.WebDocument;

@ClassDescription(label="torrentproject.se")
public class TorrentProjectSE extends DownloadFinder implements MagazineProvider, MusicAlbumFinder, GameFinder, BookFinder {

	private static final String BASE_URL = "http://torrentproject.se";

	@Override
	public void configureProvider() throws Exception {
	}
	
	protected List<SearchResult> extractResults( WebDocument document ) throws IOException, URISyntaxException {
		Elements nodes = document.jsoup(".torrent");
		List<SearchResult> results = new ArrayList<>();
		for (Element element : nodes) {
			Element link = element.select("h3 a").first();
			String title = link.text();
			float sizeInMegs = parseSize( element.select(".torrent-size").first().text() );
			String referer = link.absUrl("href");
			
			WebDocument torrentDocument = client.getDocument( referer, HTTPClient.REFRESH_ONE_DAY );
			
			String url = null;
			
			Element magnetLink = torrentDocument.jsoupSingle("a[href*=magnet:]");
			if (magnetLink != null) {
				url = magnetLink.attr("href");
			} else {
				Element torrentLink = torrentDocument.jsoupSingle("#download a[href*=.torrent]");
				url = torrentLink.attr("href");
			}
			
			results.add( new SearchResult(this, SearchResultType.TORRENT, title, url, referer, sizeInMegs) );
		}
		return results;
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {
		return extractResults( client.getDocument(String.format("%s/?s=%s&filter=3000", BASE_URL, searchString(issueSearchString)), HTTPClient.REFRESH_ONE_DAY));
	}

	@Override
	public List<SearchResult> findMusicAlbum(String artist, String album, MusicQuality quality) throws MusicAlbumSearchException {
		try {
			return extractResults( client.getDocument(String.format("%s/?s=%s+%s&filter=1000", BASE_URL, searchString(artist), searchString(album)), HTTPClient.REFRESH_ONE_DAY));
		} catch (IOException | URISyntaxException e) {
			throw new MusicAlbumSearchException( e );
		}
	}
	
	@Override
	public List<SearchResult> findGame(VideoGame videoGame) throws Exception {
		String additionalParams = "";
		String filter = "6000";
		
		switch (videoGame.getPlatform()) {
		case PS1:
			additionalParams = "-PSP";
			filter = "6103";
			break;
		case PSP:
			filter = "6103";
			break;
		case PS2:
			filter = "6103";
			break;
		case PS3:
			filter = "6103";
			break;
		case NINTENDO_GAMECUBE:
			filter = "6102";
			break;
		case NINTENDO_WII:
			filter = "6102";
			break;

		default:
			break;
		}

		return extractResults( client.getDocument(String.format("%s/?s=%s+%s&filter=%s", BASE_URL, searchString(videoGame.getName()), additionalParams, filter), HTTPClient.REFRESH_ONE_DAY));
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return true;
	}

	@Override
	public List<SearchResult> findBook(Book book) throws Exception {
		return extractResults( client.getDocument(String.format("%s/?s=%s+%s&filter=1000", BASE_URL, searchString(book.getAuthor()), searchString(book.getName())), HTTPClient.REFRESH_ONE_DAY));
	}

}
