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
import dynamo.core.configuration.ClassDescription;
import dynamo.core.configuration.Configurable;
import dynamo.finders.core.EpisodeFinder;
import dynamo.finders.core.GameFinder;
import dynamo.finders.core.MovieProvider;
import dynamo.finders.core.TVShowSeasonProvider;
import dynamo.finders.music.MusicAlbumFinder;
import dynamo.finders.music.MusicAlbumSearchException;
import dynamo.games.model.VideoGame;
import dynamo.magazines.MagazineProvider;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;

@ClassDescription(label="KickAssTorrents")
public class KATProvider extends DownloadFinder implements EpisodeFinder, MusicAlbumFinder, TVShowSeasonProvider, MovieProvider, MagazineProvider, GameFinder {

	@Configurable(ifExpression="KATProvider.enabled", defaultValue="http://kat.am")
	private String baseURL = "http://kat.am";
	
	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	private WebDocument getDocument( String searchParams, int pageNumber ) throws IOException, URISyntaxException {
		searchParams = searchParams.replace("!", "");
		String searchURL = baseURL + "/usearch/" + searchParams + "/" + pageNumber + "?field=seeders&sorder=desc";
		return client.getDocument( searchURL, baseURL + "/", HTTPClient.REFRESH_ONE_HOUR );
	}

	private List<SearchResult> findDownloadsForURL( String searchParams ) throws IOException, URISyntaxException {
		List<SearchResult> results = new ArrayList<SearchResult>();
		WebDocument	document = getDocument( searchParams, 1 );
		if ( document.jsoupSingle("p:contains(did not match any documents)") == null ) {
			Elements elements = document.jsoup("tr[id*=torrent_]");
			for (Element element : elements) {
				String title = element.select("a[class*=cellMainLink]").text();
				String downloadTorrentLink = element.select("a[title*=Download torrent file]").attr("abs:href");
				
				String sizeExpression = element.select("td[class*=nobr]").text();
	
				results.add( new SearchResult( this, SearchResultType.TORRENT, title, downloadTorrentLink, document.getOriginalURL().toString(), parseSize(sizeExpression)	) );
			}
		}

		return results;
	}

	@Override
	public List<SearchResult> findEpisode(String searchString, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception {
		List<SearchResult> results = new ArrayList<>();
		String searchParams = String.format("%s S%02dE%02d seeds:1", searchString, seasonNumber, episodeNumber);
		results.addAll(  findDownloadsForURL( searchParams ) );
		return results;
	}

	@Override
	public List<SearchResult> findMusicAlbum( String artist, String album, MusicQuality quality ) throws MusicAlbumSearchException {
		String searchParams = String.format("%s %s category:music seeds:1", artist, album );
		try {
			return findDownloadsForURL( searchParams );
		} catch (Exception e) {
			throw new MusicAlbumSearchException( e );
		}

	}

	@Override
	public List<SearchResult> findDownloadsForSeason(String searchString, Language audioLanguage, int seasonNumber) throws Exception {
		//TODO: improve ( found some with S%02d )
		String searchParams = null;
		if ( audioLanguage != null ) {
			searchParams = String.format("%s Season %d %s seeds:1", searchString, seasonNumber, audioLanguage.getShortName());
		} else  {
			searchParams = String.format("%s Season %d seeds:1", searchString, seasonNumber);
		}
		return findDownloadsForURL( searchParams );
	}

	@Override
	public void configureProvider() {
	}

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality, Language audioLanguage, Language subtitlesLanguage)
			throws Exception {
		String category = "movies";
		if ( videoQuality.equals( VideoQuality._1080p ) || videoQuality.equals( VideoQuality._720p )) {
			category = "highres-movies";
		}
		String searchParams = String.format("%s %d category:%s seeds:1", name, year, category);
		return findDownloadsForURL( searchParams );
	}

	@Override
	public List<SearchResult> findEpisode(String searchString, Language audioLanguage, int absoluteEpisodeNumber) throws Exception {
		String searchParams = String.format("%s %d seeds:1", searchString, absoluteEpisodeNumber);
		return findDownloadsForURL( searchParams );
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {
		String searchParams = String.format("%s category:books seeds:1", issueSearchString);
		return findDownloadsForURL( searchParams );
	}

	@Override
	public List<SearchResult> findGame(VideoGame videoGame) throws Exception {
		String searchParams = String.format("%s %s category:games seeds:1", videoGame.getName(), videoGame.getPlatform().name());
		return findDownloadsForURL( searchParams );
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return true;
	}
}
