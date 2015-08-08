package dynamo.providers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.finders.core.GameFinder;
import dynamo.finders.core.MovieProvider;
import dynamo.finders.music.MusicAlbumFinder;
import dynamo.finders.music.MusicAlbumSearchException;
import dynamo.model.games.GamePlatform;
import dynamo.model.games.VideoGame;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;

public class OMGTorrentComProvider extends DownloadFinder implements MovieProvider, GameFinder, MusicAlbumFinder {

	public OMGTorrentComProvider() {
		super("http://www.omgtorrent.com");
	}

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality, Language audioLanguage, Language subtitlesLanguage)
			throws Exception {

		String searchURL = String.format("%s/recherche/?order=seeders&orderby=desc&query=%s+%d#nav", getRootURL(), plus(name), year);
		WebDocument document = client.getDocument( searchURL, HTTPClient.REFRESH_ONE_DAY );
		
		List<SearchResult> results = new ArrayList<SearchResult>();
		
		extractResults(videoQuality, null, document, results);
	
		return results;
	}

	private void extractResults(VideoQuality wantedVideoQuality, String wantedCategory,
			WebDocument document, List<SearchResult> results )
			throws IOException, URISyntaxException {
		while (document != null) {
			Elements elements = document.jsoup("tr[class*=table_corps]");
			for (Element element : elements) {
				String category = element.child(0).text().trim();
				String title = element.child(1).text();
				String size = element.child(2).text();
				
				String url = element.child(1).select("a").attr("abs:href");
				
				if (wantedCategory != null && !wantedCategory.equalsIgnoreCase( category )) {
					continue;
				}
				
				if (wantedVideoQuality != null) {
					VideoQuality foundQuality = VideoQuality.findMatch( category );
					if (foundQuality != null && !foundQuality.equals(wantedVideoQuality)) {
						continue;
					}
				}

				WebDocument torrentDocument = client.getDocument(url, HTTPClient.REFRESH_ONE_DAY );
				String torrentURL = torrentDocument.jsoupSingle("a:contains(Télécharger le torrent)").absUrl("href");
				results.add( new SearchResult( this, SearchResultType.TORRENT, title, torrentURL, url, parseSize(size), false ) );
			}

			Element nextPageLink = document.jsoupSingle("a:contains(Suiv)");
			document = nextPageLink	!= null ? client.getDocument( nextPageLink.absUrl("href"), HTTPClient.REFRESH_ONE_DAY ) : null;
		}
	}

	@Override
	public void configureProvider() {
	}

	@Override
	public String getLabel() {
		return "OMG Torrent";
	}
	
	@Override
	public List<SearchResult> findGame(VideoGame videoGame) throws Exception {
		List<SearchResult> results = new ArrayList<SearchResult>();
		
		String searchURL = String.format("%s/recherche/?order=seeders&orderby=desc&query=%s", getRootURL(), plus(videoGame.getName()) );
		
		if ( videoGame.getPlatform() == GamePlatform.PC) {
			
			WebDocument document = client.getDocument( searchURL, HTTPClient.REFRESH_ONE_DAY );
			extractResults(null, "Jeux PC", document, results);
		}
		
		return results;
	}

	@Override
	public List<SearchResult> findMusicAlbum( String artist, String album, MusicQuality quality ) throws MusicAlbumSearchException {
		List<SearchResult> results = new ArrayList<SearchResult>();
		
		try {
			String searchURL = String.format("%s/recherche/?order=seeders&orderby=desc&query=%s", getRootURL(), plus( String.format("%s %s", artist, album )) );
			WebDocument document = client.getDocument( searchURL, HTTPClient.REFRESH_ONE_DAY );
			extractResults(null, "Albums", document, results);
		} catch (IOException | URISyntaxException e) {
			throw new MusicAlbumSearchException( e );
		}
	
		return results;
	}

}
