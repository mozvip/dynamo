package dynamo.services;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;

import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;

@Path("movie-db")
public class MovieDBService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<MovieInfo> find(@QueryParam("name") String name, @QueryParam("year") int year, @QueryParam("lang") String language) throws MovieDbException {
		ResultList<MovieInfo> result = MovieManager.getInstance().search(name, year, Language.getByShortName( language ));
		if (result.getTotalResults() > 0) {
			result.getResults().stream().forEach(movie -> {
				movie.setOriginalLanguage(movie.getOriginalLanguage() != null ? movie.getOriginalLanguage().toUpperCase() : "EN");
				if (StringUtils.isNotEmpty( movie.getPosterPath() )) {
					try {
						movie.setPosterPath( MovieManager.getInstance().getImageURL( movie.getPosterPath()) );
					} catch (MovieDbException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			});
			return result.getResults().stream().collect( Collectors.toList() );
		}
		return null;
	}
	
	@PUT
	public Movie selectMovie(@QueryParam("id") long id, @QueryParam("movieDbId") int movieDbId) throws MovieDbException, IOException {
		MovieInfo movieDb = MovieManager.getInstance().getMovieInfo( movieDbId );
		return MovieManager.getInstance().associate(id, movieDb);
	}

}
