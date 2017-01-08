package com.github.dynamo.providers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.finders.core.EpisodeFinder;
import com.github.dynamo.finders.core.MovieProvider;
import com.github.dynamo.finders.core.TVShowSeasonProvider;
import com.github.dynamo.finders.music.MusicAlbumFinder;
import com.github.dynamo.finders.music.MusicAlbumSearchException;
import com.github.dynamo.magazines.MagazineProvider;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultType;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.parsers.ParsedMovieInfo;
import com.github.dynamo.parsers.VideoNameParser;
import com.github.dynamo.suggesters.movies.MovieSuggester;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebDocument;

@ClassDescription(label="CPasBien")
public class CPasBienProvider extends DownloadFinder implements MovieProvider, EpisodeFinder, MusicAlbumFinder, TVShowSeasonProvider, MagazineProvider, MovieSuggester {

	private static final String BASE_URL = "http://www.cpasbien.cm";
	
	private List<SearchResult> extractResults( WebDocument document, String urlMatchRegexp ) throws IOException, URISyntaxException {
		List<SearchResult> results = new ArrayList<SearchResult>();
		while (document != null ) {
			Elements nodeResults = document.jsoup("tr[class^=color]");
			for (Element node : nodeResults) {
				String title = node.select("a[class=lien-rechercher]").text().trim();
				String sizeExpression = node.select("td[class=poid]").text();
						
				String url = node.select("a").attr("abs:href");
				if ( urlMatchRegexp == null || RegExp.matches( url, urlMatchRegexp) ) {
					String torrentURL = BASE_URL + "/_torrents" + url.substring(url.lastIndexOf('/')).replace(".html", ".torrent");
					results.add( new SearchResult( this, SearchResultType.TORRENT, title, torrentURL, url, parseSize(sizeExpression) ) );
				}
			}
			
			Element nextPageLink = document.jsoupSingle(".pagination a:contains(Suiv)");
			if (nextPageLink != null) {
				document = client.getDocument( nextPageLink.absUrl("href"), HTTPClient.REFRESH_ONE_DAY);
			} else {
				document = null;
			}
		}
		
		return results;		

	}

	private List<SearchResult> extractResults( String searchParam, String urlMatchRegexp ) throws Exception {
		WebDocument currentDocument = client.post(BASE_URL+"/recherche/", BASE_URL, "champ_recherche="+searchParam).getDocument();
		return extractResults( currentDocument, urlMatchRegexp );
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception {
		return extractResults( String.format("%s S%02dE%02d", seriesName, seasonNumber, episodeNumber), ".*/series/.*" );
	}

	@Override
	public List<SearchResult> findMusicAlbum( String artist, String album, MusicQuality quality ) throws MusicAlbumSearchException {
		try {
			return extractResults( String.format("%s %s %s", artist, album, quality == MusicQuality.LOSSLESS ? "FLAC" : "" ), ".*/musique/.*" );
		} catch (Exception e) {
			throw new MusicAlbumSearchException( e );
		}
	}

	@Override
	public List<SearchResult> findDownloadsForSeason(String seriesName,
			Language audioLanguage, int seasonNumber) throws Exception {
		return extractResults( String.format("%s Saison %d", seriesName, seasonNumber), ".*/series/.*" );
	}

	@Override
	public void configureProvider() {
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine( String issueSearchString ) throws Exception {
		return extractResults( issueSearchString, ".*/ebook/.*" );
	}

	@Override
	public List<SearchResult> findEpisode(String searchString, Language audioLanguage, int absoluteEpisodeNumber) throws Exception {
		return extractResults( String.format("%s %d", searchString, absoluteEpisodeNumber), ".*/series/.*" );
	}

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality,
			Language audioLanguage, Language subtitlesLanguage)
			throws Exception {
		List<SearchResult> results = new ArrayList<SearchResult>();
		for (String qualityAlias : videoQuality.getAliases()) {
 			results.addAll( extractResults( String.format("%s %d %s", name, year, qualityAlias), ".*/films/.*" ) );
		}
		return results; 
	}

	@Override
	public void suggestMovies() throws Exception {
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get( Calendar.YEAR );
		
		WebDocument currentDocument = client.getDocument( String.format("%s/recherche/1080p/%d/page-0", BASE_URL, year), HTTPClient.REFRESH_ONE_DAY);

		int pagesToRetrieve = 4;
		int currentPage = 0;
		
		while (currentDocument != null && currentPage < pagesToRetrieve ) {
			
			List<SearchResult> results = extractResults(currentDocument, null);
			for (SearchResult searchResult : results) {
				ParsedMovieInfo info = VideoNameParser.getMovieInfo( searchResult.getTitle() );
				if (info != null) {
					try {
						MovieManager.getInstance().suggestByName(info.getName(), year, null, Language.FR, false, null);
					} catch (Exception e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			}
			currentPage ++;
			currentDocument = client.getDocument( String.format("%s/recherche/1080p/%d/page-%d", BASE_URL, year, currentPage), HTTPClient.REFRESH_ONE_DAY);
		}
	}
	
	@Override
	public boolean needsLanguageInSearchString() {
		return false;
	}

}
