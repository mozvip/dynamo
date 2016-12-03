package dynamo.providers.magazines;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.configuration.ClassDescription;
import dynamo.core.manager.ErrorManager;
import dynamo.magazines.KioskIssuesSuggester;
import dynamo.magazines.KioskIssuesSuggesterException;
import dynamo.magazines.MagazineManager;
import dynamo.model.DownloadLocation;
import dynamo.model.DownloadSuggestion;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;

@ClassDescription(label="EBookW")
public class EBookW implements KioskIssuesSuggester {
	
	private final static int MAX_PAGES = 10;

	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		for (int i=1; i<=MAX_PAGES; i++) {
			extractFromPage(i);
		}
	}
	
	public void extractFromPage(int i) throws KioskIssuesSuggesterException {
		String url = String.format("http://ebookw.net/magazines/page/%d", i);
		WebDocument document;
		HTTPClient client = HTTPClient.getInstance();
		try {
			document = client.getDocument( url, HTTPClient.REFRESH_ONE_DAY );
		} catch (IOException e) {
			throw new KioskIssuesSuggesterException( e );
		}
		Elements shortNewsList = document.jsoup("#dle-content .shortnews");
		Elements footerList = document.jsoup("#dle-content .foot");
		int index = 0;
		for (Element shortNews : shortNewsList) {
			
			String[] attributes = shortNews.select(".text-center").first().ownText().split("\\|");
			
			String languageStr = attributes[0];
			Language language = Language.getByFullName( languageStr );
			
			String sizeExpression = attributes[ attributes.length -1 ];
			
			float size = DownloadFinder.parseSize(sizeExpression);
			
			Elements imageElement = shortNews.select("img");
			if (imageElement != null && imageElement.size() > 0) {
				String coverImage = imageElement.first().absUrl("src");
				String title = imageElement.attr("alt");

				Element footer = footerList.get(index);
				String magazineURL = footer.select(".readmore a").first().absUrl("href");
				
				Set<DownloadLocation> locations = extractLocations(magazineURL, url);

				MagazineManager.getInstance().suggest( new DownloadSuggestion(title, coverImage, url, locations, language, size, getClass(), false, magazineURL));
			}
			index++;
		}
	}

	public Set<DownloadLocation> extractLocations(String magazineURL, String referer ) {
		Set<DownloadLocation> locations = new HashSet<>();
		try {
			WebDocument magazinePage = HTTPClient.getInstance().getDocument( magazineURL, referer, HTTPClient.REFRESH_ONE_MONTH );
			String[] links = magazinePage.jsoup("#download_links").text().split("\\s");
			
			for (String link : links) {
				locations.add( new DownloadLocation(SearchResultType.HTTP, link));
			}

		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		return locations;
	}

}
