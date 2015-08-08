package dynamo.trakt;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface TraktService {
	
	@GET("/oauth/authorize?response_type=code&client_id=1f93ab28686a87d36c0e198f15a34ba7c0d3fb45cbd3303515da246718b570a6&redirect_uri=urn:ietf:wg:oauth:2.0:oob")
	void authorize( @Path("username") String username );

	@POST("/oauth/token")
	TraktTokenResponse token(@Body TraktTokenRequest request);

	@GET("/recommendations/movies")
	List<TraktMovie> recommandationsMovies();

	@GET("/users/{username}/watched/movies")
	List<TraktWatchedEntry> moviesWatched( @Path("username") String username );

	@GET("/users/{username}/watched/shows")
	List<TraktWatchedEntry> showsWatched( @Path("username") String username );
	
	@GET("/users/{username}/watchlist/movies")
	List<TraktWatchListEntry> moviesWatchList( @Path("username") String username );

}
