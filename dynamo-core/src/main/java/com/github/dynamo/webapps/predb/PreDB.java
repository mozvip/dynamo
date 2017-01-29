package com.github.dynamo.webapps.predb;

import java.util.Collection;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.parsers.ParsedMovieInfo;
import com.github.dynamo.parsers.VideoNameParser;
import com.github.dynamo.suggesters.movies.MovieSuggester;
import com.github.mozvip.predb.PreDBClient;
import com.github.mozvip.predb.model.PreDBCategory;
import com.github.mozvip.predb.model.PreDBPost;
import com.github.mozvip.predb.model.PreDBPostDetails;

@ClassDescription(label="PreDB")
public class PreDB implements MovieSuggester {
	
	private int MAX_PAGE_FOR_SUGGESTION = 3;
	
	PreDBClient client = new PreDBClient();

	@Override
	public void suggestMovies() throws Exception {

		for (int i=1; i<=MAX_PAGE_FOR_SUGGESTION; i++) {
			
			Collection<PreDBPost> posts = client.getPostsForPageTagsCats(i, null, PreDBCategory.MOVIES);
			for (PreDBPost post : posts) {
				PreDBPostDetails postDetails = client.getPostDetails( post.getId() );
				ParsedMovieInfo movieInfo = VideoNameParser.getMovieInfo( postDetails.getRlsName() );
				if (movieInfo != null ) {
					MovieManager.getInstance().suggestByName(movieInfo.getName(), movieInfo.getYear(), null, Language.EN, false, postDetails.baseUri());
				} else {
					ErrorManager.getInstance().reportWarning( String.format("Unable to parse movie release info from String %s", postDetails.getRlsName() ));
				}
			}
		}
	}	

}
