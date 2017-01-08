package com.github.dynamo.suggesters.books;

import com.github.dynamo.core.Language;

public class AmazonFRBooksBestSellers extends AmazonRSSBookSuggester {

	@Override
	public Language getLanguage() {
		return Language.FR;
	}
	
	@Override
	public String getRSSURL() {
		return "http://www.amazon.fr/gp/rss/bestsellers/books/ref=zg_bs_books_rsslink";
	}
	
	@Override
	public String toString() {
		return "amazon.fr books best sellers";
	}

}
