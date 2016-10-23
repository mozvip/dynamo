package dynamo.providers.magazines;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.Language;
import dynamo.core.configuration.ClassDescription;
import dynamo.magazines.KioskIssuesSuggester;
import dynamo.magazines.KioskIssuesSuggesterException;
import dynamo.magazines.MagazineManager;
import dynamo.model.DownloadLocation;
import dynamo.model.DownloadSuggestion;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;

@ClassDescription(label="RLSBB.com")
public class RLSBBCom implements KioskIssuesSuggester {

	private final static int MAX_PAGES = 10;

	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		for (int i=1; i<=MAX_PAGES; i++) {
			String url = String.format("http://rlsbb.com/category/ebooks-magazines/page/%d", i);
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
				
				Set<DownloadLocation> downloadLocations = new HashSet<>();
				Elements links = element.select(".postContent a");
				for (Element link : links) {
					downloadLocations.add( new DownloadLocation(SearchResultType.HTTP, link.absUrl("href") ));
				}
				
				Element readMoreLink = titles.select("a.postReadMore").first();

				MagazineManager.getInstance().suggest( new DownloadSuggestion(title, coverImage, url, downloadLocations, Language.EN, -1.0f, toString(), null, false, readMoreLink.absUrl("href")));
			}
		}
	}
	
	@Override
	public String toString() {
		return "http://rlsbb.com";
	}
	

}
