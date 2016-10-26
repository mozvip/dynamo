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
				document = HTTPClient.getInstance().getDocument( url, HTTPClient.REFRESH_ONE_DAY );
			} catch (IOException e) {
				throw new KioskIssuesSuggesterException( e );
			}
			Elements shortNewsList = document.jsoup("#dle-content .shortnews");
			Elements footerList = document.jsoup("#dle-content .foot");
			int index = 0;
			for (Element shortNews : shortNewsList) {
				Elements imageElement = shortNews.select("img");
				
				if (imageElement != null && imageElement.size() > 0) {
					String coverImage = imageElement.first().absUrl("src");
					String title = imageElement.attr("title");
	
					Element footer = footerList.get(index);
					String suggestionURL = footer.select(".readmore a").first().absUrl("href");

					MagazineManager.getInstance().suggest( new DownloadSuggestion(title, coverImage, url, null, null, -1.0f, getClass(), false, suggestionURL));
				}
				index++;
			}
		}
	}
	
	@Override
	public String toString() {
		return "EBookW.com";
	}
	

}
