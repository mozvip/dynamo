package dynamo.webapps.itunes_charts;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.manager.MusicManager;
import dynamo.suggesters.music.MusicAlbumSuggester;
import hclient.HTTPClient;

public class ITunesCharts implements MusicAlbumSuggester {
	
	public String getURL() {
		//http://www.apple.com/fr/itunes/charts/albums/ for France : make this configurable ?
		return "http://www.apple.com/itunes/charts/albums/";
	}

	@Override
	public void suggestAlbums() throws Exception {
		
		String referer = getURL();

		WebDocument document = HTTPClient.getInstance().getDocument(referer, HTTPClient.REFRESH_ONE_DAY);
		
		Elements elements = document.jsoup(".main .section-content li");
		for (Element element : elements) {
			Element img = element.select("img").first();
			String albumName = img.attr("alt");
			String artistName = element.select("h4").first().text();
			String imageURL = img.absUrl("src");
			
			// FIXME : extract genre

			MusicManager.getInstance().suggest(artistName, albumName, null, imageURL, referer);
		}
	}
	
	@Override
	public String toString() {
		return "iTunes Chart";
	}
	

}
