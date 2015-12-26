package dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import dynamo.model.DownloadableStatus;
import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;

@Path("movies")
public class MoviesService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Movie> findByStatus( @QueryParam("status") DownloadableStatus status ) {
		if (status == null) {
			status = DownloadableStatus.DOWNLOADED;
		}
		return MovieManager.getInstance().findByStatus(status);
	}
}
