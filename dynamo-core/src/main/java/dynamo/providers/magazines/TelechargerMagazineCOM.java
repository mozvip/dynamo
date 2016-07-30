package dynamo.providers.magazines;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.RegExp;
import core.WebDocument;
import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.magazines.KioskIssuesSuggester;
import dynamo.magazines.KioskIssuesSuggesterException;
import dynamo.magazines.MagazineManager;
import dynamo.model.DownloadLocation;
import dynamo.model.DownloadSuggestion;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;

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
				
				try {
					
					WebDocument magazinePage = HTTPClient.getInstance().getDocument( link.absUrl("href"), url, HTTPClient.REFRESH_ONE_WEEK );
					Elements downloadLinks = magazinePage.jsoup("a:contains(Télécharger)");
					Set<DownloadLocation> downloadLocations = new HashSet<>();
					for (Element element : downloadLinks) {
						downloadLocations.add( new DownloadLocation(SearchResultType.HTTP, element.absUrl("href")));
					}
					MagazineManager.getInstance().suggest( new DownloadSuggestion(title, coverImage, url, downloadLocations, Language.FR, -1.0f, toString(), null, false, link.absUrl("href")));
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
