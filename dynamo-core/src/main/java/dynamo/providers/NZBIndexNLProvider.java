package dynamo.providers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.backlog.tasks.nzb.DownloadNZBTask;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.EpisodeFinder;
import dynamo.finders.core.MovieProvider;
import dynamo.finders.music.MusicAlbumFinder;
import dynamo.finders.music.MusicAlbumSearchException;
import dynamo.magazines.MagazineProvider;
import dynamo.model.movies.MovieManager;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;

public class NZBIndexNLProvider extends DownloadFinder implements MovieProvider, EpisodeFinder, MusicAlbumFinder, MagazineProvider {

	public NZBIndexNLProvider() {
		super("http://nzbindex.nl");
	}
	
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

	@Override
	public String getLabel() {
		return "NZBIndex";
	}	

	private List<SearchResult> extractResults( String searchURL ) throws IOException, URISyntaxException {
		List<SearchResult> results = new ArrayList<SearchResult>();
		
		WebDocument document = client.getDocument( searchURL, HTTPClient.REFRESH_ONE_DAY );
		Elements elements = document.jsoup("div#results tr[class~=odd|even]");
		for (Element element : elements) {
		
			String title = element.select("label").text();
			String nzbURL = element.select("a[href*=/download/]").get(0).absUrl("href");
			String size = element.select("td.nowrap div").get(0).text();
			
			SearchResult result = new SearchResult(this, SearchResultType.NZB, title, nzbURL, searchURL, parseSize(size), false );
			results.add( result );
		}

		return results;
	}

	@Override
	public List<SearchResult> findMovie( String name, int year, VideoQuality videoQuality,
			Language audioLanguage, Language subtitlesLanguage )
			throws Exception {
		
		int minimumSize = MovieManager.getInstance().getMinimumSizeForMovie(videoQuality);
		
		String searchURL = String.format( "%s/search/?q=%s+-PASSWORDED&age=&max=250&minsize=%d&maxsize=&sort=agedesc&dq=&poster=&nfo=&complete=1&hidespam=0&hidespam=1&more=1", rootURL, plus(name), minimumSize );
		return extractResults( searchURL );
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode(String seriesName, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception {
		int minimumSize = 100;
		String searchURL = String.format( "%s/search/?q=%s+S%02dE%02d+-PASSWORDED&age=&max=250&minage=&sort=agedesc&minsize=%d&maxsize=&dq=&poster=&nfo=&complete=1&hidespam=0&hidespam=1&more=1",
				rootURL, plus(seriesName), seasonNumber, episodeNumber, minimumSize );
		return extractResults( searchURL );
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode(String seriesName, Language audioLanguage, int absoluteEpisodeNumber) throws Exception {
		int minimumSize = 100;
		String searchURL = String.format( "%s/search/?q=%s+%d+-PASSWORDED&age=&max=250&minage=&sort=agedesc&minsize=%d&maxsize=&dq=&poster=&nfo=&complete=1&hidespam=0&hidespam=1&more=1",
				rootURL, plus(seriesName), absoluteEpisodeNumber, minimumSize );
		return extractResults( searchURL );
	}

	@Override
	public List<SearchResult> findMusicAlbum(String artist, String album, MusicQuality quality) throws MusicAlbumSearchException {
		int minimumSize = 40;
		try {
			String searchURL = String.format( "%s/search/?q=%s+%s+-PASSWORDED&age=&max=250&minage=&sort=agedesc&minsize=%d&maxsize=&dq=&poster=&nfo=&complete=1&hidespam=0&hidespam=1&more=1",
					rootURL, plus(artist), plus(album), minimumSize );
			return extractResults( searchURL );
		} catch (IOException | URISyntaxException e) {
			throw new MusicAlbumSearchException( e );
		}
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {
		int minimumSize = 30;
		String searchURL = String.format( "%s/search/?q=%s+-PASSWORDED&age=&max=250&minage=&sort=agedesc&minsize=%d&maxsize=&dq=&poster=&nfo=&complete=1&hidespam=0&hidespam=1&more=1",
				rootURL, plus(issueSearchString), minimumSize );
		return extractResults( searchURL );
	}

}
