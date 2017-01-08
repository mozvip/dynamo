package com.github.dynamo.trakt;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dynamo.core.Enableable;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.manager.ConfigAnnotationManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.suggesters.movies.MovieSuggester;
import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.SimpleResponse;
import com.github.mozvip.hclient.core.WebDocument;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import com.uwetrottmann.trakt5.entities.BaseMovie;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.entities.Movie;
import com.uwetrottmann.trakt5.entities.UserSlug;
import com.uwetrottmann.trakt5.enums.Extended;

import retrofit2.Response;

@ClassDescription(label = "Trakt")
public class TraktManager implements Reconfigurable, MovieSuggester, Enableable {

	@Configurable
	private boolean enabled;

	@Configurable(ifExpression = "TraktManager.enabled", required = true)
	private String username;

	@Configurable(ifExpression = "TraktManager.enabled", required = true)
	private String password;

	public static final String CLIENT_ID = "1f93ab28686a87d36c0e198f15a34ba7c0d3fb45cbd3303515da246718b570a6";
	private static final String CLIENT_SECRET = "6c2324d35586037c7940b0d021a8ed88995a87ff964514011876d4ba03b2459d";

	private UserSlug userSlug;

	private TraktV2 trakt;

	private TraktManager() {
		trakt = new TraktV2(CLIENT_ID, CLIENT_SECRET, "urn:ietf:wg:oauth:2.0:oob");
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@JsonIgnore
	public List<Movie> getMovieRecommandations() throws IOException, OAuthSystemException, URISyntaxException {
		Response<List<Movie>> response = trakt.recommendations().movies(Extended.FULL).execute();
		return response.body();
	}

	private void auth() throws OAuthSystemException, IOException, URISyntaxException {
		userSlug = new UserSlug(username);

		String state = UUID.randomUUID().toString();

		OAuthClientRequest buildAuthorizationRequest = trakt.buildAuthorizationRequest(state);

		String authorizeURI;

		SimpleResponse response = HTTPClient.getInstance().get(buildAuthorizationRequest.getLocationUri());
		if (response.getLastRedirectLocationURL().endsWith("/signin")) {
			WebDocument signinPage = response.getDocument();
			response = HTTPClient.getInstance().submit(signinPage.jsoupSingle("form[action*=signin]"),
					"user[login]=" + username, "user[password]=" + password);
		}
		authorizeURI = response.getLastRedirectLocationURL();

		String authCode = authorizeURI.substring(authorizeURI.lastIndexOf('/') + 1).toUpperCase();
		Response<AccessToken> resp = trakt.exchangeCodeForAccessToken(authCode);
		String access_token = resp.body().access_token;
		ConfigAnnotationManager.getInstance().setConfigString("TraktManager.access_token", access_token);
		if (access_token != null) {
			trakt.accessToken(resp.body().access_token);
		}
	}

	@JsonIgnore
	public List<BaseMovie> getMoviesWatched() throws IOException {
		Response<List<BaseMovie>> response = trakt.users().watchedMovies(userSlug, Extended.FULL).execute();
		return response.body();
	}

	@JsonIgnore
	public List<BaseShow> getShowsWatched() throws IOException {
		Response<List<BaseShow>> response = trakt.users().watchedShows(userSlug, Extended.FULL).execute();
		return response.body();
	}

	@Override
	public void reconfigure() {
		if (!enabled) {
			return;
		}

		try {
			auth();
		} catch (OAuthSystemException | IOException | URISyntaxException e) {
			ErrorManager.getInstance().reportThrowable(e);
			setEnabled(false);
		}
	}

	@Override
	public void suggestMovies() throws Exception {
		Response<List<BaseMovie>> response = trakt.users().watchlistMovies(userSlug, Extended.FULL).execute();
		List<BaseMovie> movies = response.body();
		for (BaseMovie movie : movies) {
			MovieManager.getInstance().suggestImdbId(movie.movie.ids.imdb, null, Language.EN, movie.movie.homepage);
		}
	}

	public String getName() {
		return "Trakt watch list";
	}

}
