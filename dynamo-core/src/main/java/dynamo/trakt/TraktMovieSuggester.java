package dynamo.trakt;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

import com.omertron.themoviedbapi.MovieDbException;

import dynamo.core.Enableable;
import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.model.movies.MovieManager;
import dynamo.suggesters.movies.MovieSuggester;

public class TraktMovieSuggester implements MovieSuggester, Enableable {
	
	@Override
	public boolean isEnabled() {
		return TraktManager.getInstance().isEnabled();
	}

	@Override
	public void suggestMovies() throws IOException, URISyntaxException {

		if (!TraktManager.getInstance().isEnabled()) {
			return;
		}
		
		List<TraktMovie> recommandations = TraktManager.getInstance().getMovieRecommandations();
		for (TraktMovie recommendation : recommandations) {
			try {
				MovieManager.getInstance().suggestImdbId( recommendation.getIds().get("imdb"), null, Language.EN, recommendation.getUrl() );
			} catch (MovieDbException | ParseException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
	}
	
	@Override
	public String toString() {
		return "Trakt Recommendations";
	}

}
