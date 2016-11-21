package dynamo.trakt;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

import com.omertron.themoviedbapi.MovieDbException;

import dynamo.core.Enableable;
import dynamo.core.Language;
import dynamo.core.configuration.ClassDescription;
import dynamo.core.manager.ErrorManager;
import dynamo.movies.model.MovieManager;
import dynamo.suggesters.movies.MovieSuggester;

@ClassDescription(label="Trakt Recommendations")
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
			} catch (MovieDbException | ParseException | InterruptedException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
	}

}
