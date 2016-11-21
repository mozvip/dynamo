package dynamo.suggesters.movies;

import core.WebResource;
import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
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
	protected void createSuggestion(String title, String contributor, String imageURL, String rssURL, String suggestionURL) throws Exception {
		title = VideoNameParser.clean(title, filtersRegExps);
		
		WebResource image = new WebResource( imageURL.toString(), rssURL.toString() );

		Movie movie = MovieManager.getInstance().suggestByName(title, 0, image, Language.FR, false, suggestionURL);
		if (movie == null) {
			ErrorManager.getInstance().reportWarning(String.format("Unable to parse movie name %s", title), true);
		}
	}

}
