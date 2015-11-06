package dynamo.providers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.RegExp;
import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.configuration.Configurable;
import dynamo.finders.core.EpisodeFinder;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;

public class EZTVProvider extends DownloadFinder implements EpisodeFinder {
	
	@Configurable(category="Providers", name="EZTV Base URL", disabled="#{!EZTVProvider.enabled}", required="#{EZTVProvider.enabled}", defaultValue="https://eztv.ag")
	private String baseURL = "https://eztv.ag";

	public String getBaseURL() {
		return baseURL;
	}
	
	public void setBaseURL(String eztvDomain) {
		this.baseURL = eztvDomain;
	}

	@Override
	public String getLabel() {
		return "EZTV";
	}

	@Override
	public void configureProvider() throws Exception {
	}

	protected List<SearchResult> search( String search ) throws IOException {
		List<SearchResult> results = new ArrayList<>();
		
		String searchString = search.replaceAll("\\s+", "-").toLowerCase();
		String referer = String.format("%s/search/%s", baseURL, searchString );
		
		WebDocument document = client.getDocument(referer);
		Elements rows = document.jsoup("tr:has(a.epinfo)");
		for (Element row : rows) {

			Element link = row.select("a.epinfo").first();

			String sizeExpression = RegExp.extract( link.attr("title") , ".*\\s+\\(([\\d\\.]+\\s+[MG]B)\\)");
			float sizeInMegs = sizeExpression != null ? parseSize(sizeExpression) : -1.0f;

			String title = link.text();
			Elements downloadLinks = row.select("a[class~=magnet|download_\\d+]");
			for (Element downloadLink : downloadLinks) {
				results.add( new SearchResult( this, SearchResultType.TORRENT, title, downloadLink.attr("href"), referer, sizeInMegs, false) );
			}
		}
		
		return results;
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode(String seriesName, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception {
		return search( String.format("%s s%02de%02d", seriesName, seasonNumber, episodeNumber));
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode(String seriesName, Language audioLanguage, int absoluteEpisodeNumber) throws Exception {
		return search( String.format("%s %d", seriesName, absoluteEpisodeNumber));
	}
	
	@Override
	public boolean needsLanguageInSearchString() {
		return true;
	}

}
