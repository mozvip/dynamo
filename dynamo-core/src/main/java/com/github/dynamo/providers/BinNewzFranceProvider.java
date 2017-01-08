package com.github.dynamo.providers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.magazines.MagazineProvider;
import com.github.dynamo.model.result.SearchResult;
import com.github.mozvip.hclient.SimpleResponse;
import com.github.mozvip.hclient.core.WebDocument;

@ClassDescription(label="BinnewZ France")
public class BinNewzFranceProvider extends DownloadFinder implements MagazineProvider {

	private static final String BASE_URL = "http://www.binnews.in/";

	@Override
	public void configureProvider() {
	}

	@Override
	public List<SearchResult> findDownloadsForMagazine(String issueSearchString) throws Exception {

		String url = BASE_URL + "/_bin/search2.php";
		
		String searchString = issueSearchString.replaceAll("\\s+", "+");
		int category = 25;
		
		extractResultsFromURL(url, searchString, category);

		return null;
	}

	protected void extractResultsFromURL(String url, String searchString, int category) throws IOException, UnsupportedEncodingException {
		SimpleResponse response = client.post( url, url, "edTitre=" + searchString, "chkInit=1", "chkTitre=on", "chkFichier=on", "chkCat=on", String.format("cats[]=%d", category), "edAge=", "edAge2=", "edYear=");
		WebDocument document = response.getDocument();
		
		Elements rows = document.jsoup("#tabliste .ligneclaire, #tabliste .lignefoncee");
		for (Element row : rows) {
			Elements cells = row.select("td");
			String title = cells.get( 2 ).text();
			Element cellFlag = cells.get(3);
			String newsgroup = cells.get(4).text();
			
			String nzbSearchString = cells.get(5).text();
			String sizeStr = cells.get(6).text();
			
			float size = parseSize( sizeStr );
			
			
		}
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return false;
	}

}
