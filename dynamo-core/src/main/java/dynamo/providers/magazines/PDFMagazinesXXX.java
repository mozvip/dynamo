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

public class PDFMagazinesXXX implements KioskIssuesSuggester {

	static int MAX_PAGE_TO_GRAB = 10;

	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		for (int page=1; page<MAX_PAGE_TO_GRAB; page++) {
			String url = String.format("http://www.pdfmagazines.xxx/magazines/page/%d", page);
			WebDocument document;
			try {
				document = HTTPClient.getInstance().getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
			} catch (IOException e) {
				throw new KioskIssuesSuggesterException( e );
			}
			Elements elements = document.jsoup("div.item-short-wrap");
			for (Element magazineElement : elements) {
				
				Element detailsLink = magazineElement.select(".data h2 > a").first();
				String title = detailsLink.text();
				String coverImage = magazineElement.select(".picture img").first().absUrl("src");
				
				// String genres
				
				// String infoURL = detailsLink.absUrl("href");
				
				MagazineManager.getInstance().suggest( new DownloadSuggestion(title, coverImage, url, null, null, -1.0f, toString(), null, true));
			}
		}
	}
	
	@Override
	public String toString() {
		return "pdfmagazines.xxx";
	}

}
