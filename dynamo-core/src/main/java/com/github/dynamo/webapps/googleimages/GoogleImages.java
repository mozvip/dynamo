package com.github.dynamo.webapps.googleimages;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.dynamo.core.manager.ErrorManager;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.SimpleResponse;
import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebDocument;
import com.github.mozvip.hclient.core.WebResource;

public class GoogleImages {
	
	public static WebResource findImage( String searchString, float ratio ) {
		
		String googleSearchString = searchString.replaceAll("[\\W]", " ");
		googleSearchString = googleSearchString.replaceAll("\\s+", " ").trim();
		googleSearchString = googleSearchString.replaceAll("\\s", "+");

		String referer = String.format("http://www.google.com/search?q=%s&tbm=isch&tbs=isz:m", googleSearchString);
		WebDocument document;
		try {
			HTTPClient client = HTTPClient.getInstance();
			document = client.getDocument( referer, HTTPClient.REFRESH_ONE_WEEK);
			if (document == null) {
				return null;
			}
			Elements images = document.jsoup("div.rg_di");
			for (Element element : images) {
				Elements links = element.select("a.rg_l[href*=imgurl]");
				if (links.size() == 0) {
					continue;
				}
				
				String href = links.first().attr("href");
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
				
				try {
					SimpleResponse simpleResponse = client.get(imageURL, imageRefURL, HTTPClient.REFRESH_ONE_HOUR);
					if (StringUtils.startsWith( simpleResponse.getContentType(), "image/")) {
						return new WebResource( imageURL, imageRefURL );
					}
				} catch (HttpHostConnectException e) {
				}
			}
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
		
		
		return null;
				
	}

}
