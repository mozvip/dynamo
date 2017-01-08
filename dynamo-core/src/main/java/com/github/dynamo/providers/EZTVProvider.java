package com.github.dynamo.providers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.finders.core.EpisodeFinder;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.model.result.SearchResultType;
import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebDocument;

@ClassDescription(label="EZTV")
public class EZTVProvider extends DownloadFinder implements EpisodeFinder {
	
	@Configurable(ifExpression="EZTVProvider.enabled", defaultValue="https://eztv.ag")
	private String baseURL = "https://eztv.ag";

	public String getBaseURL() {
		return baseURL;
	}
	
	public void setBaseURL(String eztvDomain) {
		this.baseURL = eztvDomain;
	}

	@Override
	public void configureProvider() throws Exception {
	}

	protected List<SearchResult> search( String search ) throws IOException {
		
		String searchChars = search.replaceAll("\\W", "").toLowerCase();
		
		List<SearchResult> results = new ArrayList<>();
		
		String searchString = search.replaceAll("\\s+", "-").toLowerCase();
		String referer = String.format("%s/search/%s", baseURL, searchString );
		
		WebDocument document = client.getDocument(referer);
		Elements rows = document.jsoup("tr:has(a.epinfo)");
		for (Element row : rows) {

			Element link = row.select("a.epinfo").first();
			String title = link.text();
			
			String titleChars = title.replaceAll("\\W", "").toLowerCase();
			
			if (!titleChars.contains( searchChars )) {
				continue;
			}

			String sizeExpression = RegExp.extract( link.attr("title") , ".*\\s+\\(([\\d\\.]+\\s+[MG]B)\\)");
			float sizeInMegs = sizeExpression != null ? parseSize(sizeExpression) : -1.0f;

			Elements downloadLinks = row.select("a[class~=magnet|download_\\d+]");
			for (Element downloadLink : downloadLinks) {
				results.add( new SearchResult( this, SearchResultType.TORRENT, title, downloadLink.attr("href"), referer, sizeInMegs) );
			}
		}
		
		return results;
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception {
		return search( String.format("%s s%02de%02d", seriesName, seasonNumber, episodeNumber));
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int absoluteEpisodeNumber) throws Exception {
		return search( String.format("%s %d", seriesName, absoluteEpisodeNumber));
	}
	
	@Override
	public boolean needsLanguageInSearchString() {
		return true;
	}

}
