package dynamo.providers;

import java.util.List;

import dynamo.core.configuration.ClassDescription;
import dynamo.games.GameSuggester;
import dynamo.games.model.GamePlatform;
import dynamo.games.model.VideoGame;
import dynamo.magazines.KioskIssuesSuggester;
import dynamo.magazines.KioskIssuesSuggesterException;
import dynamo.model.ebooks.books.BookSuggester;

@ClassDescription(label="RLSLog.net")
public class RLSLog implements KioskIssuesSuggester, BookSuggester, GameSuggester {
	
	public void extractFromFeed( String feedURL ) {
		
	}

	@Override
	public List<VideoGame> suggestGames(GamePlatform platform) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void suggestBooks() throws Exception {
		extractFromFeed("http://www.rlslog.net/category/ebooks/ebook/feed/");
	}

	@Override
	public void suggestIssues() throws KioskIssuesSuggesterException {
		extractFromFeed("http://www.rlslog.net/category/ebooks/magazines/feed/");		
	}



}
