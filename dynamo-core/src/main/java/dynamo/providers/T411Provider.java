package dynamo.providers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.RegExp;
import core.WebDocument;
import core.WebResource;
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
import dynamo.magazines.KioskIssuesSuggester;
import dynamo.magazines.KioskIssuesSuggesterException;
import dynamo.magazines.MagazineManager;
import dynamo.magazines.MagazineProvider;
import dynamo.model.DownloadLocation;
import dynamo.model.DownloadSuggestion;
import dynamo.model.ebooks.books.Book;
import dynamo.model.ebooks.books.BookFinder;
import dynamo.model.ebooks.books.BookManager;
import dynamo.model.ebooks.books.BookSuggester;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.suggesters.movies.MovieSuggester;
import dynamo.utils.images.CoverImageFinder;
import dynamo.webapps.googleimages.GoogleImages;
import hclient.HTTPClient;

@ClassDescription(label="Torrent 411")
public class T411Provider extends DownloadFinder implements BookFinder, EpisodeFinder, TVShowSeasonProvider, MusicAlbumFinder, MovieProvider, MagazineProvider, GameFinder, KioskIssuesSuggester, BookSuggester, MovieSuggester {

	@Configurable(ifExpression="T411Provider.enabled", required=true)
	private String login;
	@Configurable(ifExpression="T411Provider.enabled", required=true)
	private String password;
	@Configurable(ifExpression="T411Provider.enabled", required=true, defaultValue="http://www.t411.in")
	private String baseURL;

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

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	@Override
	public void configureProvider() throws Exception {
		WebDocument loginPage = client.getDocument(baseURL + "/users/login/", 0);
		if (loginPage.jsoupSingle("a.logout") == null) { // not already logged in
			WebDocument newPage = client.submit( loginPage.jsoupSingle("form#loginform"), "login="+login, "password="+password ).getDocument();
			if ( newPage.jsoupSingle("a.logout") == null) {
				ErrorManager.getInstance().reportError("Login failed for " + toString() + ", disabling provider");
				setEnabled( false );
			}
		}
	}

	protected List<SearchResult> extractResults( String searchURL, int pages ) throws Exception {
		
		searchURL += "&order=added&type=desc";

		List<SearchResult> results = new ArrayList<SearchResult>();
		WebDocument document = client.getDocument( searchURL, baseURL + "/", HTTPClient.REFRESH_ONE_HOUR );

		int currentPage = 0;
		while (document != null) {
			Elements rows = document.jsoup( "table.results tbody tr" );
			for (Element row : rows) {
	
				Element link = row.select("td:gt(0) a[href*=/torrents/]").first();
				if (link == null) {
					continue;
				}
	
				String href = link.attr("abs:href");
				String title = link.attr("title");
				String size = row.child(5).text();
	
				Element nfoLink = row.select("a.nfo").first();
				String torrentId = RegExp.extract( nfoLink.attr("href"), ".*id=(\\d+)" );
				String torrentURL = String.format( "%s/torrents/download/?id=%s", baseURL, torrentId );
	
				results.add( new SearchResult( this, SearchResultType.TORRENT, title, torrentURL, href, parseSize(size) ));
			}
			
			Element nextLink = getLinkToNextPage(document);
			if ( (pages == -1 || currentPage < pages) && nextLink != null ) {
				document = client.getDocument(nextLink.absUrl("href"), HTTPClient.REFRESH_ONE_HOUR );
			} else {
				document = null;
			}
			
			currentPage ++;
		}

		return results;		
		
	}

	protected List<SearchResult> searchVideo( String search, Language audioLanguage, int subcat, String additionalParams ) throws Exception {
		String languageSuffix = "";
		if ( audioLanguage == Language.EN ) {
			languageSuffix = "&term[17][]=540&term[17][]=721";
		} else if ( audioLanguage == Language.FR ) {
			languageSuffix = "&term[17][]=541&term[17][]=542&term[17][]=720";
		}

		search = URLEncoder.encode(search, "UTF-8");
		String searchURL = String.format( baseURL + "/torrents/search/?search=%s&cat=210&submit=Recherche&subcat=%d%s", search, subcat, languageSuffix);
		
		if (additionalParams != null) {
			searchURL += "&" + additionalParams;
		}

		return extractResults( searchURL, 1 );
	}

	protected List<SearchResult> searchMusicAlbum( String search, String additionalParams ) throws Exception {

		search = URLEncoder.encode(search, "UTF-8");
		String searchURL = String.format( "%s/torrents/search/?search=%s&cat=395&submit=Recherche&subcat=623", baseURL, search );
		
		if (additionalParams != null) {
			searchURL += "&" + additionalParams;
		}

		return extractResults( searchURL, 1 );
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode( String searchString, Language audioLanguage, int seasonNumber, int episodeNumber ) throws Exception {

		String searchParams = String.format("%s S%02dE%02d", searchString, seasonNumber, episodeNumber);
		
		String additionalParams = "term[45][]=" + ( 967 + seasonNumber );
		additionalParams += "&term[46][]=" + ( 936 + episodeNumber );

		List<SearchResult> results = new ArrayList<SearchResult>();
		results.addAll( searchVideo( searchParams, audioLanguage, 433, additionalParams ) );
		results.addAll( searchVideo( searchParams, audioLanguage, 637, additionalParams ) );
		return results;
	}

	@Override
	public List<SearchResult> findDownloadsForSeason( String seriesName, Language audioLanguage, int seasonNumber ) throws Exception {
		
		String additionalParams = "term[45][]=" + ( 967 + seasonNumber );
		additionalParams += "&term[46][]=936";
				
		List<SearchResult> results = new ArrayList<SearchResult>();
		results.addAll( searchVideo( seriesName, audioLanguage, 433, additionalParams ) );
		results.addAll( searchVideo( seriesName, audioLanguage, 637, additionalParams ) );

		return results;
	}

	@Override
	public List<SearchResult> findMusicAlbum( String artist, String album, MusicQuality quality ) throws MusicAlbumSearchException {

		String searchParams = String.format("%s %s", artist, album );

		List<SearchResult> results = new ArrayList<SearchResult>();
		
		String additionalParams = ( quality == MusicQuality.LOSSLESS ? "term[16][]=529" : null );
		try {
			results.addAll( searchMusicAlbum(searchParams, additionalParams) );
		} catch (Exception e) {
			throw new MusicAlbumSearchException( e );
		}

		return results;
	}

	@Override
	public List<SearchResult> findMovie( String name, int year, VideoQuality videoQuality, Language audioLanguage, Language subtitlesLanguage ) throws Exception {
		String searchName = year > 0 ? name + " " + year : name;
		return searchVideo(searchName, audioLanguage, 631, null);
	}
	
	@Override
	public List<SearchResult> findDownloadsForMagazine( String issueSearchString ) throws Exception {

		issueSearchString = URLEncoder.encode(issueSearchString, "UTF-8");
		String searchURL = String.format( baseURL + "/torrents/search/?search=%s&submit=Recherche&subcat=410&order=added&type=desc", issueSearchString );

		return extractResults( searchURL, 1 );
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode(String searchString, Language audioLanguage, int absoluteEpisodeNumber) throws Exception {
		String searchParams = String.format("%s %d", searchString, absoluteEpisodeNumber);

		List<SearchResult> results = new ArrayList<SearchResult>();
		results.addAll( searchVideo( searchParams, audioLanguage, 433, null ) );
		results.addAll( searchVideo( searchParams, audioLanguage, 637, null ) );
			return results;
		}

	@Override
	public List<SearchResult> findGame( VideoGame videoGame ) throws Exception {
		String search = URLEncoder.encode(videoGame.getName(), "UTF-8");
		
		String term = "";
		int subcat = 246;	// PC

		switch (videoGame.getPlatform()) {
		case XBOX360:
			subcat = 309;
			term = "&term[36][]=705";
			break;
		case NINTENDO_3DS:
			subcat = 307;
			term = "&term[37][]=702";
			break;
		case NINTENDO_DS:
			subcat = 307;
			term = "&term[37][]=701";
			break;
		case NINTENDO_GAMECUBE:
			subcat = 307;
			term = "&term[37][]=738";
			break;
		case NINTENDO_WII:
			subcat = 307;
			term = "&term[37][]=703";
			break;
		case PS1:
			subcat = 308;
			term = "&term[18][]=613";
			break;
		case PS2:
			subcat = 308;
			term = "&term[18][]=614";
			break;
		case PS3:
			subcat = 308;
			term = "&term[18][]=617";
			break;
		case PSP:
			subcat = 308;
			term = "&term[18][]=615";
			break;

		default:
			break;
		}

		String searchURL = String.format( "%s/torrents/search/?search=%s&cat=624&submit=Recherche&subcat=%d&order=added&type=desc" + term, baseURL, search, subcat );

		return extractResults( searchURL, 1 );
	}
	
	public List<DownloadSuggestion> extractSuggestions( String startingURL, int pagesToRetrieve, boolean retrieveImages ) throws IOException, URISyntaxException {
		String currentURL = startingURL;

		List<DownloadSuggestion> suggestions = new ArrayList<>();

		WebDocument document = client.getDocument( currentURL, HTTPClient.REFRESH_ONE_HOUR );

		int currentPage = 0;
		while (document != null && currentPage < pagesToRetrieve) {
			
			Elements rows = document.jsoup( "table.results tbody tr" );
			for (Element row : rows) {
	
				Element link = row.select( "td:gt(0) a[href*=/torrents/]").first();
				if (link == null) {
					continue;
				}
	
				String href = link.attr("abs:href");
				String title = link.attr("title");
				String size = row.child(5).text();
	
				Element nfoLink = row.select("a.nfo").first();
				String torrentId = RegExp.extract( nfoLink.attr("href"), ".*id=(\\d+)" );
				String torrentURL = String.format( "%s/torrents/download/?id=%s", baseURL, torrentId );

				String imageSrc = null;

				if (retrieveImages) {
					WebDocument torrentDocument = client.getDocument(href, currentURL, HTTPClient.REFRESH_ONE_WEEK);
					if (torrentDocument.getResponseCode() != 404) {
						try {
							imageSrc = findCoverImage( title, torrentDocument, currentURL );
						} catch ( IOException | URISyntaxException e ) {
							ErrorManager.getInstance().reportThrowable(e);
						}
					}
				}
				Set<DownloadLocation> downloadLocations = new HashSet<>();
				downloadLocations.add( new DownloadLocation(SearchResultType.TORRENT, torrentURL));
				try {
					suggestions.add( new DownloadSuggestion(title, imageSrc, href, downloadLocations, Language.FR, parseSize(size), toString(), getClass(), false, href) );
				} catch (Exception e) {
					ErrorManager.getInstance().reportThrowable(e);
				}
			}
			
			Element nextLink = getLinkToNextPage(document);
			if (nextLink == null) {
				break;
			}
			currentURL = nextLink.absUrl("href");
			document = nextLink != null ? client.getDocument(currentURL, HTTPClient.REFRESH_ONE_HOUR ) : null;
			currentPage ++;
		}
		
		return suggestions;
	}

	private Element getLinkToNextPage(WebDocument document) {
		return document.jsoupSingle("a[title=Suivant Page]");
	}

	public String findCoverImage( String title, WebDocument torrentDocument, String referer ) throws IOException, URISyntaxException {

		String imageSrc = null;

		Elements images = torrentDocument.jsoup("article img");
		List<Element> elligibleImages = filterImages(images);

		imageSrc = CoverImageFinder.getInstance().selectCoverImage(elligibleImages, 0.7f, 200);
		if (imageSrc == null) {
			WebResource resource = GoogleImages.findImage( title, 0.7f);
			imageSrc = resource != null ? resource.getUrl() : null;	// FIXME: referer ?
		}
		
		return imageSrc;

	}

	private List<Element> filterImages(Elements allImages) {
		List<Element> elligibleImages = new ArrayList<>();
		for (Element element : allImages) {
			if (element.attr("src").isEmpty()) {
				continue;
			}
			if (element.attr("src").startsWith("/")) {
				continue;
			}
			if (element.attr("src").startsWith("http://www.dream-prez.com/")) {
				continue;
			}
			elligibleImages.add( element );
		}
		if (elligibleImages.size() > 1) {
			// hack to remove last image which is never the correct one
			elligibleImages = elligibleImages.subList(0, elligibleImages.size() - 1);
		}
		return elligibleImages;
	}
	
	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		List<DownloadSuggestion> magazineSuggestions;
		try {
			magazineSuggestions = extractSuggestions(String.format("%s/torrents/search/?subcat=410&order=added&type=desc", baseURL), 10, true);
			for (DownloadSuggestion magazineSuggestion : magazineSuggestions) {
				MagazineManager.getInstance().suggest( magazineSuggestion );
			}
		} catch (IOException | URISyntaxException e) {
			throw new KioskIssuesSuggesterException( e );
		}
	}

	@Override
	public void suggestBooks() throws Exception {
		List<DownloadSuggestion> bookSuggestions = extractSuggestions(String.format("%s/torrents/search/?subcat=408&order=added&type=desc", baseURL), 10, true);
		for (DownloadSuggestion bookSuggestion : bookSuggestions) {
			BookManager.getInstance().suggest( bookSuggestion );
		}
	}

	@Override
	public List<SearchResult> findBook(Book book) throws Exception {
		String searchURL = String.format("%s/torrents/search/?search=%s&subcat=408&order=added&type=desc", baseURL, plus(book.getName()) );
		return extractResults( searchURL, 1 );		
	}

	@Override
	public void suggestMovies() throws Exception {
		List<DownloadSuggestion> movieSuggestions = extractSuggestions(String.format("%s/torrents/search/?search=&subcat=631&term[7][]=16", baseURL), 2, false);
		for (DownloadSuggestion movieSuggestion : movieSuggestions) {
			String titleToParse = movieSuggestion.getTitle();
			titleToParse = titleToParse.replaceAll("1080p", "");

			String[] groups = RegExp.parseGroups( titleToParse, "([\\w\\s\\.]+)(\\d{4})(.*)");
			if (groups != null) {
				String movieName =  groups[0].trim();
				movieName = movieName.replaceAll("\\.", " ").trim();
				Movie movie = MovieManager.getInstance().suggestByName( movieName, Integer.parseInt( groups[1] ), null, Language.FR, false, movieSuggestion.getReferer());
				if (movie != null) {
					// TODO: store search result
				}
			}
		}
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return false;
	}

}
