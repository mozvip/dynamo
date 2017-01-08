package com.github.dynamo.providers.magazines;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.magazines.KioskIssuesSuggester;
import com.github.dynamo.magazines.KioskIssuesSuggesterException;
import com.github.dynamo.magazines.MagazineManager;
import com.github.dynamo.model.DownloadLocation;
import com.github.dynamo.model.DownloadSuggestion;
import com.github.dynamo.model.result.SearchResultType;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.WebDocument;

@ClassDescription(label="RLSBB.com")
public class RLSBBCom implements KioskIssuesSuggester {

	private final static int MAX_PAGES = 10;

	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		for (int i=1; i<=MAX_PAGES; i++) {
			extractFromPage("http://rlsbb.com/category/ebooks-magazines/page/%d", i);
		}
	}

	public WebDocument extractFromPage(String baseURL, int i) throws KioskIssuesSuggesterException {
		String url = String.format(baseURL, i);
		WebDocument document;
		try {
			document = HTTPClient.getInstance().getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
		} catch (IOException e) {
			throw new KioskIssuesSuggesterException( e );
		}
		Elements titles = document.jsoup("div.post");
		for (Element element : titles) {
			String title = element.select("h2>a").first().text();
			String coverImage = element.select(".postContent img").first().absUrl("src");
			
			String[] attributes = element.select(".postContent p").last().ownText().split("\\|");
			
			String sizeStr = attributes[ attributes.length -1 ];
			float size = DownloadFinder.parseSize( sizeStr );
			
			Language language = Language.getByFullName( attributes[0] );
			
			Set<DownloadLocation> downloadLocations = new HashSet<>();
			Elements links = element.select(".postContent a");
			for (Element link : links) {
				downloadLocations.add( new DownloadLocation(SearchResultType.HTTP, link.absUrl("href") ));
			}
			
			Element readMoreLink = titles.select("a.postReadMore").first();

			MagazineManager.getInstance().suggest( new DownloadSuggestion(title, coverImage, url, downloadLocations, language, size, getClass(), false, readMoreLink.absUrl("href")));
		}
		return document;
	}

}
