package dynamo.providers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.WebDocument;

import dynamo.core.DownloadFinder;
import dynamo.core.configuration.ClassDescription;
import dynamo.finders.music.MusicAlbumFinder;
import dynamo.finders.music.MusicAlbumSearchException;
import dynamo.games.GameFinder;
import dynamo.games.model.VideoGame;
import dynamo.magazines.MagazineProvider;
import dynamo.model.ebooks.books.Book;
import dynamo.model.ebooks.books.BookFinder;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;

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
		return extractResults( client.getDocument(String.format("%s/?s=%s&filter=3000", BASE_URL, plus(issueSearchString)), HTTPClient.REFRESH_ONE_DAY));
	}

	@Override
	public List<SearchResult> findMusicAlbum(String artist, String album, MusicQuality quality) throws MusicAlbumSearchException {
		try {
			return extractResults( client.getDocument(String.format("%s/?s=%s+%s&filter=1000", BASE_URL, plus(artist), plus(album)), HTTPClient.REFRESH_ONE_DAY));
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

		return extractResults( client.getDocument(String.format("%s/?s=%s+%s&filter=%s", BASE_URL, plus(videoGame.getName()), additionalParams, filter), HTTPClient.REFRESH_ONE_DAY));
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return true;
	}

	@Override
	public List<SearchResult> findBook(Book book) throws Exception {
		return extractResults( client.getDocument(String.format("%s/?s=%s+%s&filter=1000", BASE_URL, plus(book.getAuthor()), plus(book.getName())), HTTPClient.REFRESH_ONE_DAY));
	}

}
