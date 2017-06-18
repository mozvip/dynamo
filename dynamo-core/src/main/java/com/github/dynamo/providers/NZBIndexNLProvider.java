package com.github.dynamo.providers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.backlog.tasks.nzb.DownloadNZBTask;
import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.manager.ConfigurationManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.finders.core.EpisodeFinder;
import com.github.dynamo.finders.core.MovieProvider;
import com.github.dynamo.finders.music.MusicAlbumFinder;
import com.github.dynamo.finders.music.MusicAlbumSearchException;
import com.github.dynamo.games.GameFinder;
import com.github.dynamo.games.model.GamePlatform;
import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.magazines.MagazineProvider;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultType;
import com.github.dynamo.movies.model.MovieManager;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.WebDocument;

@ClassDescription(label="NZBIndex.nl")
public class NZBIndexNLProvider extends DownloadFinder implements MovieProvider, EpisodeFinder, MusicAlbumFinder, MagazineProvider, GameFinder {

	private static final String BASE_URL = "http://nzbindex.nl";
	private static final String SEARCH_SUFFIX = "+-PASSWORDED&age=&max=250&minage=&sort=agedesc&minsize=%d&maxsize=&dq=&poster=&nfo=&complete=1&hidespam=1&more=1";
	
	@Override
	public boolean isEnabled() {
		// FIXME : find a better way, maybe a NZBDownloadFinder super class ?
		return super.isEnabled() && ConfigurationManager.getInstance().getActivePlugin(DownloadNZBTask.class) != null;
	}

	@Override
	public void configureProvider() {
		try {
			client.post("http://nzbindex.nl/agree/", "http://nzbindex.nl/", "agree=I+agree");
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable( e );
			setEnabled( false );
		}
	}

	private List<SearchResult> extractResults( String searchURL ) throws IOException, URISyntaxException {
		List<SearchResult> results = new ArrayList<SearchResult>();
		
		WebDocument document = client.getDocument( searchURL, HTTPClient.REFRESH_ONE_DAY );
		Elements elements = document.jsoup("div#results tr[class~=odd|even]");
		for (Element element : elements) {
		
			String title = element.select("label").text();
			String nzbURL = element.select("a[href*=/download/]").get(0).absUrl("href");
			String size = element.select("td.nowrap div").get(0).text().replace(",", "");
			
			SearchResult result = new SearchResult(this, SearchResultType.NZB, title, nzbURL, searchURL, parseSize(size) );
			results.add( result );
		}

		return results;
	}

	@Override
	public List<SearchResult> findMovie( String name, int year, VideoQuality videoQuality,
			Language audioLanguage, Language subtitlesLanguage )
			throws Exception {
		
		int minimumSize = MovieManager.getInstance().getMinimumSizeForMovie(videoQuality);
		
		String searchURL = String.format( "%s/search/?q=%s+%d" + SEARCH_SUFFIX, BASE_URL, searchString(name), year, minimumSize );
		List<SearchResult> results = extractResults( searchURL );
		if (videoQuality != null && videoQuality != VideoQuality.SD) {
			return results.stream().filter( movie -> StringUtils.containsAny( movie.getTitle(), videoQuality.getAliases() )).collect( Collectors.toList() );
		} else {
			return results;
		}
	}

	@Override
	public List<SearchResult> findEpisode(String searchString, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception {
		int minimumSize = 100;
		String searchURL = String.format( "%s/search/?q=%s+S%02dE%02d" + SEARCH_SUFFIX, BASE_URL, searchString(searchString), seasonNumber, episodeNumber, minimumSize );
		return extractResults( searchURL );
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int absoluteEpisodeNumber) throws Exception {
		int minimumSize = 100;
		String searchURL = String.format( "%s/search/?q=%s+%d" + SEARCH_SUFFIX, BASE_URL, searchString(seriesName), absoluteEpisodeNumber, minimumSize );
		return extractResults( searchURL );
	}

	@Override
	public List<SearchResult> findMusicAlbum(String artist, String album, MusicQuality quality) throws MusicAlbumSearchException {
		int minimumSize = 40;
		try {
			
			album = album.replaceAll("\'", "");
			
			String searchURL = String.format( "%s/search/?q=%s+%s" + SEARCH_SUFFIX,
					BASE_URL, searchString(artist), searchString(album), minimumSize );
			return extractResults( searchURL );
		} catch (IOException | URISyntaxException e) {
			throw new MusicAlbumSearchException( e );
		}
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {
		int minimumSize = 30;
		String searchURL = String.format( "%s/search/?q=%s" + SEARCH_SUFFIX,
				BASE_URL, searchString(issueSearchString), minimumSize );
		return extractResults( searchURL );
	}

	@Override
	public List<SearchResult> findGame(VideoGame videoGame) throws Exception {
		
		int minSizeInMbs = 500;
		int[] groups = new int[] {};
		
		if ( videoGame.getPlatform() == GamePlatform.XBOX360 ) {
			groups= new int[] { 113 };
			minSizeInMbs = 5000;
		} else if ( videoGame.getPlatform() == GamePlatform.NINTENDO_WII ) {
			groups= new int[] { 116 };
			minSizeInMbs = 1000;
		} else if ( videoGame.getPlatform() == GamePlatform.NINTENDO_GAMECUBE ) {
			groups= new int[] { 752 };
			minSizeInMbs = 500;
		} else if (videoGame.getPlatform() == GamePlatform.PS2) {
			groups= new int[] { 29, 31, 32 };
			minSizeInMbs = 1000;
		} else {
			return null;	// FIXME
		}
		
		String searchURL = String.format( "%s/search/?q=%s&age=&max=25", BASE_URL, searchString( videoGame.getName() ) );
		for (int group : groups) {
			searchURL = searchURL + "&g[]=" + group;
		}
		searchURL = searchURL + String.format( "&minage=&sort=agedesc&minsize=%d&maxsize=&dq=&poster=&nfo=&hidespam=1&more=1", minSizeInMbs );

		return extractResults( searchURL );
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return true;
	}

}
