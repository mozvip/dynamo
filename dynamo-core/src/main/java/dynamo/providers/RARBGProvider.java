package dynamo.providers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import core.RegExp;
import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.configuration.ClassDescription;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.EpisodeFinder;
import dynamo.finders.core.GameFinder;
import dynamo.finders.core.MovieProvider;
import dynamo.finders.core.TVShowSeasonProvider;
import dynamo.finders.music.MusicAlbumFinder;
import dynamo.finders.music.MusicAlbumSearchException;
import dynamo.games.model.VideoGame;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadLocation;
import dynamo.model.ebooks.books.Book;
import dynamo.model.ebooks.books.BookFinder;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.suggesters.movies.MovieSuggester;

@ClassDescription(label="RARBG")
public class RARBGProvider extends DownloadFinder implements MovieSuggester, GameFinder, MovieProvider, EpisodeFinder, TVShowSeasonProvider, BookFinder, MusicAlbumFinder {
	
	private final static int MAX_PAGES = 10;
	
	private final WebClient webClient = new WebClient();
	
	@Configurable(ifExpression="RARBGProvider.enabled", defaultValue="https://rarbg.to")
	private String baseURL = "https://rarbg.to";
	
	public String getBaseURL() {
		return baseURL;
	}
	
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
	
	protected WebDocument getDocument( String url ) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		try {
			Thread.sleep( 500 );
		} catch (InterruptedException e) {
		}
		Page webPage = webClient.getPage( url );
		return new WebDocument(url, webPage.getWebResponse().getContentAsString());
	}

	@Override
	public void suggestMovies() throws Exception {
		
		for( int page=1; page<=MAX_PAGES; page++) {
			String url = String.format("%s/torrents.php?category=movies&page=%d", baseURL, page);
			WebDocument currentPage = getDocument(url);
			
			Elements rows = currentPage.jsoup("tr.lista2");
			for (Element element : rows) {
				Element torrentLink = element.child(1).select("a").first();
				
				Element imdbLink = null;
				String imdbId = null;
				Elements imdbLinks = element.select("a[href*=?imdb=]");
				if (imdbLinks.size() > 0) {
					imdbLink = imdbLinks.first();
					imdbId = RegExp.extract( imdbLink.attr("href"), ".*imdb=(\\w+)"); 
				}
				
				String title = torrentLink.attr("title");
				String torrentPageURL = torrentLink.absUrl("href");
				String size = element.child(3).text();
				
				if (imdbId != null) {
					Movie suggestion = MovieManager.getInstance().suggestImdbId(imdbId, null, Language.EN, torrentPageURL);
					if (suggestion == null) {
						continue;
					}
					
					WebDocument torrentPageDocument = getDocument( torrentPageURL );
					
					Element torrentDownloadLink = torrentPageDocument.jsoupSingle("a[href*=/download.php?id=]");
					if (torrentDownloadLink != null) {
					
						DownloadLocation dl = new DownloadLocation( SearchResultType.TORRENT, torrentDownloadLink.absUrl("href") );
						// TODO
						// Collection downloadLocations = new ArrayList<>();
						// Elements relatedRows = currentPage.jsoup("tr.lista2");
						try {
							DownloadableManager.getInstance().saveDownloadLocation(suggestion.getId(), title, this.getClass(), torrentPageURL, parseSize(size), dl);
						} catch (Exception e) {
							ErrorManager.getInstance().reportThrowable( e );
						}
					}
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
	
	protected List<SearchResult> extractResultsFromURL( String url ) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		
		List<SearchResult> results = new ArrayList<>();
		
		WebDocument currentPage = getDocument( url );	
		Elements rows = currentPage.jsoup("tr.lista2");
		for (Element element : rows) {
			Element torrentLink = element.child(1).select("a").first();
			String title = torrentLink.attr("title");
			String torrentPageURL = torrentLink.absUrl("href");
			String size = element.child(3).text();
			
			Page torrentPage = webClient.getPage(torrentPageURL);
			WebDocument torrentPageDocument = new WebDocument(url, torrentPage.getWebResponse().getContentAsString());
			
			Element torrentDownloadLink = torrentPageDocument.jsoupSingle("a[href*=/download.php?id=]");
			if (torrentDownloadLink != null) {
				results.add( new SearchResult( this, SearchResultType.TORRENT, title, torrentDownloadLink.absUrl("href"), torrentPageURL, parseSize(size)) );
			}
		}
		
		return results;
	}

	@Override
	public List<SearchResult> findGame(VideoGame videoGame) throws Exception {
		
		int[] categories = new int[] {};
		
		switch (videoGame.getPlatform()) {
		case PS3:
			categories = new int[] { 40 };
			break;

		case XBOX360:
			categories = new int[] { 32 };
			break;

		case PC:
			categories = new int[] { 27, 28 };
			break;

		default:
			break;
		}

		return extractResultsFromURL(buildURL(videoGame.getName(), categories));
	}

	private String buildURL(String searchString, int[] categories) {
		String url = baseURL + "/torrents.php?search=" + searchString;
		for (int category : categories) {
			url += "&category[]=" + category;
		}
		return url;
	}

	@Override
	public List<SearchResult> findBook(Book book) throws Exception {
		return extractResultsFromURL( buildURL( book.getName(), new int[] { 35 } ));
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int seasonNumber,
			int episodeNumber) throws Exception {
		return extractResultsFromURL( buildURL( String.format("%s S%02dE%02d", seriesName, seasonNumber, episodeNumber), new int[] { 18, 41 } ));
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage,
			int absoluteEpisodeNumber) throws Exception {
		return extractResultsFromURL( buildURL( String.format("%s %d", seriesName, absoluteEpisodeNumber), new int[] { 18, 41 } ));
	}

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality, Language audioLanguage,
			Language subtitlesLanguage) throws Exception {
		return extractResultsFromURL( buildURL( String.format("%s %d", name, year), new int[] { 14, 17, 42, 44, 45, 46, 47, 48 } ));
	}

	@Override
	public List<SearchResult> findDownloadsForSeason(String seriesName, Language audioLanguage, int seasonNumber)
			throws Exception {
		return extractResultsFromURL( buildURL( String.format("%s S%02d", seriesName, seasonNumber), new int[] { 18, 41 } ) );
	}

	@Override
	public List<SearchResult> findMusicAlbum(String artist, String album, MusicQuality quality)
			throws MusicAlbumSearchException {
		int category = ( quality == MusicQuality.COMPRESSED ? 23 : 25 );
		try {
			return extractResultsFromURL( buildURL( String.format("%s %s", artist, album), new int[] { category } ) );
		} catch (Exception e) {
			throw new MusicAlbumSearchException( e );
		}
	}

}
