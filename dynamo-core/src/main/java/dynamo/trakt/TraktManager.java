package dynamo.trakt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import dynamo.core.Enableable;
import dynamo.core.Language;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ConfigurationManager;
import dynamo.model.movies.MovieManager;
import dynamo.suggesters.movies.MovieSuggester;
import hclient.RetrofitClient;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class TraktManager implements Reconfigurable, MovieSuggester, Enableable {
	
	@Configurable(category="Trakt", name="Enable Trakt Support", bold=true)
	private boolean enabled;
	
	@Configurable(category="Trakt", name="Trakt Username", required="#{TraktManager.enabled}", disabled="#{!TraktManager.enabled}")
	private String username;

	public static String clientId = "1f93ab28686a87d36c0e198f15a34ba7c0d3fb45cbd3303515da246718b570a6";
	private String clientSecret = "6c2324d35586037c7940b0d021a8ed88995a87ff964514011876d4ba03b2459d";

	private TraktService service;

	private TraktManager() {

	}

	static class SingletonHolder {
		static TraktManager instance = new TraktManager();
	}

	public static TraktManager getInstance() {
		return SingletonHolder.instance;
	}	

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<TraktMovie> getMovieRecommandations() throws ClientProtocolException, UnsupportedEncodingException, IOException {
		return service.recommandationsMovies();
	}

	public List<TraktWatchedEntry> getMoviesWatched() throws ClientProtocolException, UnsupportedEncodingException, IOException {
		return service.moviesWatched( username );
	}

	public List<TraktWatchedEntry> getShowsWatched() throws ClientProtocolException, UnsupportedEncodingException, IOException {
		return service.showsWatched( username );
	}
	
	private TraktTokenResponse tokenResponse;
	private String access_token;

	@Override
	public void reconfigure() {
		if (!enabled) {
			return;
		}
		access_token = ConfigurationManager.getInstance().getConfigString("TraktManager.access_token");
		if (access_token != null) {
			RestAdapter restAdapter = new RestAdapter.Builder()
			.setEndpoint("https://api-v2launch.trakt.tv")
			.setRequestInterceptor( new RequestInterceptor() {
				@Override
				public void intercept(RequestFacade request) {
					request.addHeader("Content-type", "application/json");
					request.addHeader("trakt-api-version", "2");
					request.addHeader("trakt-api-key", clientId);
					request.addHeader("Authorization", "Bearer " + access_token);
				}
			})
			.setClient( new RetrofitClient() ).build();
			service = restAdapter.create(TraktService.class);
		} else {
			RestAdapter restAdapter = new RestAdapter.Builder()
			.setEndpoint("https://api-v2launch.trakt.tv")
			.setRequestInterceptor( new RequestInterceptor() {
				@Override
				public void intercept(RequestFacade request) {
					request.addHeader("Content-type", "application/json");
					request.addHeader("trakt-api-key", clientId);
					request.addHeader("trakt-api-version", "2");
				}
			})
			.setClient( new RetrofitClient() ).build();
			service = restAdapter.create(TraktService.class);
		}
	}

	@Override
	public void suggestMovies() throws Exception {
		List<TraktWatchListEntry> watchList = service.moviesWatchList( username );
		for (TraktWatchListEntry watchListEntry : watchList) {
			MovieManager.getInstance().suggestByImdbID( watchListEntry.getMovie().getIds().get("imdb"), null, Language.EN );
		}
	}
	
	public String getName() {
		return "Trakt watch list";
	}
	
	@Override
	public String toString() {
		return "Trakt";
	}
	
	public void auth( String oauth ) {
		tokenResponse = service.token( new TraktTokenRequest( oauth, clientId, clientSecret) );
		Date expirationDate = new Date( tokenResponse.getCreated_at() + tokenResponse.getExpires_in() );
		ConfigurationManager.getInstance().setConfigString("TraktManager.access_token", tokenResponse.getAccess_token());
		reconfigure();
	}

}
