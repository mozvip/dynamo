package dynamo.providers;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.RegExp;
import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.finders.core.MovieProvider;
import dynamo.magazines.MagazineProvider;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;

public class PirateBayProvider extends DownloadFinder implements MovieProvider, MagazineProvider {

	public PirateBayProvider() {
		super("http://thepiratebay.se");
	}

	@Override
	public String getLabel() {
		return "The Pirate Bay";
	}

	@Override
	public void configureProvider() {
	}

	@Override
	public List<SearchResult> findMovie( String name, int year,
			VideoQuality videoQuality, Language audioLanguage,
			Language subtitlesLanguage ) throws Exception {

		String searchString = String.format( "%s %d", name, year );
		
		searchString = searchString.replace(" ", "%20");
		
		int subcat = 201;
		if (videoQuality.equals( VideoQuality._720p ) || videoQuality.equals( VideoQuality._1080p )) {
			subcat = 207;
		}
		
		String searchURL = String.format("%s/search/%s/0/7/%d", rootURL, searchString, subcat);
		
		WebDocument document = client.getDocument( searchURL, HTTPClient.REFRESH_ONE_DAY );
		
		List<SearchResult> searchResults = new ArrayList<>();
		
		Elements rows = document.jsoup("#searchResult tbody tr");
		if (rows != null) {
			for (Element row : rows) {
				Element link = row.select(".detLink").first();
				String title = link.text();
				
				String desc = row.select(".detDesc").text();
				
				String sizeExpr = RegExp.extract( desc, ".*Size (.*)iB, .*");
				Element downloadTorrentLink = row.select("[title='Download this torrent']").first();
				
				String url = null;
				if (downloadTorrentLink != null) {
					url = downloadTorrentLink.absUrl("href");
				} else {
					url = row.select("a[href*=magnet]").first().attr("href");
				}
				
				searchResults.add( new SearchResult( this, SearchResultType.TORRENT, title,
						url, searchURL, parseSize(sizeExpr), false ) );
			}
		}
		
		return searchResults;
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
