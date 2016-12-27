package dynamo.trakt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Test;

import com.uwetrottmann.trakt5.entities.BaseMovie;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.entities.Movie;

import dynamo.tests.AbstractDynamoTest;

public class TraktManagerTest extends AbstractDynamoTest {
	
	private static TraktManager traktManager = TraktManager.getInstance();

	@Test
	public void testGetMovieRecommandations() throws ClientProtocolException, UnsupportedEncodingException, IOException, OAuthSystemException, URISyntaxException {
		List<Movie> movieRecommandations = traktManager.getMovieRecommandations();
		for (Movie movie : movieRecommandations) {
			System.out.println( movie.title );
		}
	}

	@Test
	public void testGetMoviesWatched() throws ClientProtocolException, UnsupportedEncodingException, IOException, OAuthSystemException, URISyntaxException {
		List<BaseMovie> movieRecommandations = traktManager.getMoviesWatched();
		for (BaseMovie movie : movieRecommandations) {
			System.out.println( movie.movie.title );
		}
	}
	
	@Test
	public void testGetShowsWatched() throws ClientProtocolException, UnsupportedEncodingException, IOException, OAuthSystemException, URISyntaxException {
		List<BaseShow> showRecommendations = traktManager.getShowsWatched();
		for (BaseShow show : showRecommendations) {
			System.out.println( show.show.title );
		}
	}	
}
