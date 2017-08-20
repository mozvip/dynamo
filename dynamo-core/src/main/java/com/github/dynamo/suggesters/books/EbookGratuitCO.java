package com.github.dynamo.suggesters.books;

import org.jsoup.select.Elements;

import com.github.dynamo.model.ebooks.books.BookSuggester;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.WebDocument;

public class EbookGratuitCO implements BookSuggester {
	
	private static String ROOT_URL = "https://www.ebook-gratuit.co/";

	@Override
	public void suggestBooks() throws Exception {
		// TODO Auto-generated method stub
		HTTPClient client = HTTPClient.getInstance();
		
		int page = 1;
		WebDocument document = client.getDocument(ROOT_URL + "/page/" + page, HTTPClient.REFRESH_ONE_HOUR);
		Elements articles = document.jsoup("article.item-list");
	}

}
