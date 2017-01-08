package com.github.dynamo.providers.magazines;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.magazines.KioskIssuesSuggester;
import com.github.dynamo.magazines.KioskIssuesSuggesterException;
import com.github.dynamo.magazines.MagazineManager;
import com.github.dynamo.model.DownloadLocation;
import com.github.dynamo.model.DownloadSuggestion;
import com.github.dynamo.model.result.SearchResultType;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebDocument;

public class TelechargerMagazineCOM implements KioskIssuesSuggester {

	static int MAX_PAGE_TO_GRAB = 10;

	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		for (int page=1; page<MAX_PAGE_TO_GRAB; page++) {
			String url = String.format("http://www.telecharger-magazine.com/page/%d/", page);
			WebDocument document;
			try {
				document = HTTPClient.getInstance().getDocument( url, HTTPClient.REFRESH_ONE_DAY );
			} catch (IOException e) {
				throw new KioskIssuesSuggesterException( e );
			}
			
			Elements elements = document.jsoup("div.movieposter");
			for (Element magazineElement : elements) {
				
				Element image = magazineElement.select("img").first();
				Element link = image.parent();
				
				String coverImage = image.absUrl("src");
				String title = RegExp.extract( image.attr("title"), "télécharger (.*)" );
				
				if (title == null) {
					continue;
				}
				
				try {
					
					WebDocument magazinePage = HTTPClient.getInstance().getDocument( link.absUrl("href"), url, HTTPClient.REFRESH_ONE_WEEK );
					Elements downloadLinks = magazinePage.jsoup("a:contains(Télécharger)");
					Set<DownloadLocation> downloadLocations = new HashSet<>();
					for (Element element : downloadLinks) {
						downloadLocations.add( new DownloadLocation(SearchResultType.HTTP, element.absUrl("href")));
					}
					MagazineManager.getInstance().suggest( new DownloadSuggestion(title, coverImage, url, downloadLocations, Language.FR, -1.0f, getClass(), false, link.absUrl("href")));
				} catch (IOException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "telecharger-magazine.com";
	}


}
