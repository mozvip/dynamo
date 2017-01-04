package dynamo.providers.magazines;

import java.io.IOException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.WebDocument;

import dynamo.core.configuration.ClassDescription;
import dynamo.magazines.KioskIssuesSuggester;
import dynamo.magazines.KioskIssuesSuggesterException;
import dynamo.magazines.MagazineManager;
import dynamo.model.DownloadSuggestion;

@ClassDescription(label="PDFMagazines.org")
public class PDFMagazinesOrg implements KioskIssuesSuggester {
	
	static int MAX_PAGE_TO_GRAB = 10;

	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		for (int page=1; page<MAX_PAGE_TO_GRAB; page++) {
			String url = String.format("http://www.pdfmagazines.org/magazines/page/%d", page);
			WebDocument document;
			try {
				document = HTTPClient.getInstance().getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
			} catch (IOException e) {
				throw new KioskIssuesSuggesterException( e );
			}
			Elements elements = document.jsoup("div.item-short-wrap");
			for (Element magazineElement : elements) {
				
				String title = magazineElement.select(".data h2 > a").text();
				String coverImage = magazineElement.select(".picture img").first().absUrl("src");
				
				// String genres
				
				String infoURL = magazineElement.select(".info a").first().absUrl("href");
				MagazineManager.getInstance().suggest( new DownloadSuggestion(title, coverImage, url, null, null, -1.0f, getClass(), false, infoURL));
			}
		}
	}
	
	@Override
	public String toString() {
		return "pdfmagazines.org";
	}

}
