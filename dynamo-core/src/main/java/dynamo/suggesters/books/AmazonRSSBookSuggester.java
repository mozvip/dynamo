package dynamo.suggesters.books;

import dynamo.core.Language;
import dynamo.model.ebooks.books.BookInfo;
import dynamo.model.ebooks.books.BookManager;
import dynamo.model.ebooks.books.BookSuggester;
import dynamo.suggesters.AmazonRSSSuggester;

public abstract class AmazonRSSBookSuggester extends AmazonRSSSuggester implements BookSuggester {
	
	public abstract Language getLanguage();

	@Override
	protected void createSuggestion(String title, String contributor, String imageURL, String rssURL) throws Exception {
		BookManager.getInstance().suggest( new BookInfo(title, contributor, getLanguage() ), imageURL, rssURL );
	}
	
	@Override
	public void suggestBooks() throws Exception {
		super.suggest(getRSSURL());
	}
	
	public abstract String getRSSURL();

}
