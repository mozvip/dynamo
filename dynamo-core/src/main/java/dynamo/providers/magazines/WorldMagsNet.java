package dynamo.providers.magazines;

import java.io.IOException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.magazines.KioskIssuesSuggester;
import dynamo.magazines.KioskIssuesSuggesterException;
import dynamo.magazines.MagazineManager;
import dynamo.model.DownloadSuggestion;
import hclient.HTTPClient;

public class WorldMagsNet implements KioskIssuesSuggester {
	
	static int MAX_PAGE_TO_GRAB = 10;

	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		for (int page=1; page<MAX_PAGE_TO_GRAB; page++) {
			String url = String.format("http://worldmags.net/page/%d/", page);
			WebDocument document;
			try {
				document = HTTPClient.getInstance().getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
			} catch (IOException e) {
				throw new KioskIssuesSuggesterException( e );
			}
			Elements elements = document.jsoup("div.news-mags");
			for (Element magazineElement : elements) {
				Element imageElt = magazineElement.select(".news-img-mags img").first();
				Element magazineLink = magazineElement.select(".news-title-mags a").first();
				String title = magazineLink.text();
				
				MagazineManager.getInstance().suggest( new DownloadSuggestion(title, imageElt.absUrl("src"), url, null, null, -1.0f, toString(), null, false, magazineLink.absUrl("href")));
			}
		}
	}
	
	@Override
	public String toString() {
		return "worldmags.net";
	}

}
