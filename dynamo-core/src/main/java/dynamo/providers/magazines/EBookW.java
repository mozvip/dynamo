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

public class EBookW implements KioskIssuesSuggester {
	
	private final static int MAX_PAGES = 10;

	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		for (int i=1; i<=MAX_PAGES; i++) {
			String url = String.format("http://ebookw.com/magazines/page/%d", i);
			WebDocument document;
			try {
				document = HTTPClient.getInstance().getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
			} catch (IOException e) {
				throw new KioskIssuesSuggesterException( e );
			}
			Elements titles = document.jsoup("#dle-content .title");
			Elements shortNewsList = document.jsoup("#dle-content .shortnews");
			for (int iTitle=0; iTitle<titles.size(); iTitle++) {
				String title = titles.get( iTitle ).select("a").first().text();
				
				Element shortNews = shortNewsList.get(iTitle);
				String coverImage = shortNews.select("img").first().absUrl("src");

				MagazineManager.getInstance().suggest( new DownloadSuggestion(title, coverImage, url, null, null, -1.0f, toString(), null, false));
			}
		}
	}
	
	@Override
	public String toString() {
		return "EBookW.com";
	}
	

}
