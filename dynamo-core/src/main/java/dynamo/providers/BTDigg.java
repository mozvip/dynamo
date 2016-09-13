package dynamo.providers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.configuration.ClassDescription;
import dynamo.magazines.MagazineProvider;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;

@ClassDescription(label="BTDigg.org")
public class BTDigg extends DownloadFinder implements MagazineProvider {

	private static final String BASE_URL = "http://btdigg.org";

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {
		String searchURL = String.format( "%s/search?q=%s", BASE_URL, plus(issueSearchString) );
		return extractResults( searchURL, 30, 500 );
	}

	private List<SearchResult> extractResults(String searchURL, int minimumSize, int maximumSize) throws IOException, URISyntaxException {
		List<SearchResult> results = new ArrayList<>();
		
		WebDocument document = client.getDocument( searchURL, HTTPClient.REFRESH_ONE_DAY );
		Elements rows = document.jsoup("#search_res table tr");
		for (Element element : rows) {
			String title = element.select(".torrent_name a").text();
			if (StringUtils.isNotBlank( title )) {
				String url = element.select("a[href*=magnet]").attr("href");
				Elements attributesNames = element.select(".attr_name");
				if (attributesNames != null && attributesNames.size() > 0) {
					String sizeExpression = attributesNames.get(0).nextElementSibling().text();
					float size = parseSize(sizeExpression);
					if (size > minimumSize && size < maximumSize) {
						results.add( new SearchResult(this, SearchResultType.TORRENT, title, url, searchURL, parseSize(sizeExpression)) );
					}
				}
			}
		}
		
		return results;
	}

	@Override
	public void configureProvider() throws Exception {
	}
	
	@Override
	public boolean needsLanguageInSearchString() {
		return false;
	}

}
