package dynamo.suggesters.movies;

import java.io.IOException;
import java.net.URISyntaxException;

import com.omertron.themoviedbapi.MovieDbException;

import core.WebResource;
import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;
import dynamo.parsers.VideoNameParser;
import dynamo.suggesters.AmazonRSSSuggester;

public class AmazonFRBestSellersDVDMovieSuggester extends AmazonRSSSuggester implements MovieSuggester {

	@Override
	public void suggestMovies() throws Exception {
		suggest( "http://www.amazon.fr/gp/rss/bestsellers/dvd/ref=zg_bs_dvd_rsslink" );
	}
	
	public String[] filtersRegExps = new String[] {
			"(.*)" + VideoNameParser.SEPARATOR_REGEXP + "\\[Blu-ray.*",
			"(.*)" + VideoNameParser.SEPARATOR_REGEXP + "\\[DVD.*",
			"(.*)" + VideoNameParser.SEPARATOR_REGEXP + "\\[Combo .*\\]",
			"(.*)" + VideoNameParser.SEPARATOR_REGEXP + "\\[.dition .*\\]",
			"(.*)" + VideoNameParser.SEPARATOR_REGEXP + "\\[Pack .*\\]",
			"(.*)" + VideoNameParser.SEPARATOR_REGEXP + "\\(Oscar.*\\)",
			"(.*)" + VideoNameParser.SEPARATOR_REGEXP + "- Edition limit.e\\s+.*",
			"(.*)" + VideoNameParser.SEPARATOR_REGEXP + "- Coffret\\s+.*"
	};
	
	@Override
	public String toString() {
		return "amazon.fr Best Sellers";
	}

	@Override
	protected void createSuggestion(String title, String contributor, String imageURL, String rssURL) throws Exception {
		try {
			title = VideoNameParser.clean(title, filtersRegExps);
			
			WebResource image = new WebResource( imageURL.toString(), rssURL.toString() );
			
			Movie movie = MovieManager.getInstance().suggestByName(title, 0, image, Language.FR, false);
			if (movie == null) {
				ErrorManager.getInstance().reportWarning(String.format("Unable to parse movie name %s", title), true);
			}
		} catch ( MovieDbException | IOException | URISyntaxException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		
	}

}
