package com.github.dynamo.providers;

import java.util.List;

import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.games.GameSuggester;
import com.github.dynamo.games.model.GamePlatform;
import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.magazines.KioskIssuesSuggester;
import com.github.dynamo.magazines.KioskIssuesSuggesterException;
import com.github.dynamo.model.ebooks.books.BookSuggester;

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
