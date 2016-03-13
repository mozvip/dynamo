package dynamo.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;

import core.RegExp;
import dynamo.core.Language;
import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;
import dynamo.services.MovieDBService.MovieResult;

@Path("movie-db")
public class MovieDBService {
	
	class MovieResult {
		public int movieDbId;
		public String name;
		public int year;
		public MovieResult(int movieDbId, String name, int year) {
			super();
			this.movieDbId = movieDbId;
			this.name = name;
			this.year = year;
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<MovieResult> find(@QueryParam("name") String name, @QueryParam("year") int year, @QueryParam("lang") String language) throws MovieDbException {
		ResultList<MovieInfo> result = MovieManager.getInstance().search(name, year, Language.getByShortName( language ));
		if (result.getTotalResults() > 0) {
			return result.getResults().stream().map( movie -> new MovieResult( movie.getId(), movie.getTitle(), parseYear(movie.getReleaseDate()) )).collect( Collectors.toList() );
		}
		return null;
	}
	
	@PUT
	public Movie selectMovie(@QueryParam("id") long id, @QueryParam("movieDbId") int movieDbId) throws MovieDbException {
		MovieInfo movieDb = MovieManager.getInstance().getMovieInfo( movieDbId );
		return MovieManager.getInstance().associate(id, movieDb);
	}

	private int parseYear(String releaseDate) {
		int year = -1;
		if (releaseDate != null) {
			String yearStr = RegExp.extract(releaseDate, "(\\d{4}).*");
			if (yearStr != null) {
				year = Integer.parseInt( yearStr );
			}
		}
		return year;
	}

}
