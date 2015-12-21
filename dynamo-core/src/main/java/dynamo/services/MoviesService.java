package dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;

@Path("movies")
public class MoviesService {
	
	@GET
	public List<Movie> getCollection() {
		return MovieManager.getInstance().getMovieCollection();
	}

}
