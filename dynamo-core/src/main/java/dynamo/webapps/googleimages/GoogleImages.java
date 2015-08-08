package dynamo.webapps.googleimages;

import java.io.IOException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.RegExp;
import core.WebDocument;
import core.WebResource;
import dynamo.core.manager.ErrorManager;
import hclient.HTTPClient;

public class GoogleImages {
	
	public static WebResource findImage( String searchString, float ratio ) {
		
		String googleSearchString = searchString.replaceAll("[\\W]", " ");
		googleSearchString = googleSearchString.replaceAll("\\s+", " ");
		googleSearchString = googleSearchString.replaceAll("\\s", "+");

		String referer = String.format("http://www.google.com/search?q=%s&tbm=isch&tbs=isz:m", googleSearchString);
		WebDocument document;
		try {
			document = HTTPClient.getInstance().getDocument( referer, HTTPClient.REFRESH_ONE_WEEK);
			if (document == null) {
				return null;
			}
			Elements images = document.jsoup("div.rg_di");
			for (Element element : images) {
				String href = element.select("a.rg_l[href*=imgurl]").first().attr("href");
				String imageURL = RegExp.extract(href, ".*imgurl=([^\\&]*).*");
				String imageRefURL = RegExp.extract(href, ".*imgrefurl=([^\\&]*).*");
				
				String[] imageInfo = RegExp.parseGroups( element.select("span.rg_ilmn").text(), "(\\d+)\\D+(\\d+) - (.*)" );
				
				float width = Float.parseFloat( imageInfo[0] );
				float height = Float.parseFloat( imageInfo[1] );
				String domain = imageInfo[2];	// maybe implement domains blacklist in the future
				
				if ("amazon.com".equals(domain)) {
					continue;
				}
				
				if (ratio > 0 && Math.abs( ratio - (width / height)) > 0.1f) { // allow 10% tolerance
					continue;
				}
				
				return new WebResource( imageURL, imageRefURL );
			}
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
		
		
		return null;
				
	}

}
