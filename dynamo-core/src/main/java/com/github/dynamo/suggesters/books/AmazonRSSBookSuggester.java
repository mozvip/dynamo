package com.github.dynamo.suggesters.books;

import com.github.dynamo.core.Language;
import com.github.dynamo.model.ebooks.books.BookInfo;
import com.github.dynamo.model.ebooks.books.BookManager;
import com.github.dynamo.model.ebooks.books.BookSuggester;
import com.github.dynamo.suggesters.AmazonRSSSuggester;

public abstract class AmazonRSSBookSuggester extends AmazonRSSSuggester implements BookSuggester {
	
	public abstract Language getLanguage();

	@Override
	protected void createSuggestion(String title, String contributor, String imageURL, String rssURL, String suggestionURL) throws Exception {
		BookManager.getInstance().suggest( new BookInfo(title, contributor, getLanguage() ), imageURL, rssURL, suggestionURL );
	}
	
	@Override
	public void suggestBooks() throws Exception {
		super.suggest(getRSSURL());
	}
	
	public abstract String getRSSURL();

}
