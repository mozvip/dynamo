package dynamo.trakt;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import com.omertron.themoviedbapi.MovieDbException;
import com.uwetrottmann.trakt5.entities.Movie;

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

		try {
			List<Movie> recommandations = TraktManager.getInstance().getMovieRecommandations();
			if (recommandations != null) {
				for (Movie recommendation : recommandations) {
					try {
						MovieManager.getInstance().suggestImdbId( recommendation.ids.imdb, null, Language.EN, recommendation.homepage );
					} catch (MovieDbException | ParseException | InterruptedException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			}
		} catch (OAuthSystemException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}

}
