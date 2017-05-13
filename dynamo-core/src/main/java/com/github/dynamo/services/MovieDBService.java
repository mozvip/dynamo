package com.github.dynamo.services;

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

import com.github.dynamo.core.Language;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.movies.model.Movie;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.movies.model.TheMovieDB;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;

@Path("movie-db")
public class MovieDBService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<MovieInfo> find(@QueryParam("name") String name, @QueryParam("year") int year, @QueryParam("lang") String language) throws MovieDbException, InterruptedException {
		ResultList<MovieInfo> result = TheMovieDB.getInstance().search(name, year, Language.getByShortName( language ));
		if (result.getTotalResults() > 0) {
			result.getResults().stream().forEach(movie -> {
				movie.setOriginalLanguage(movie.getOriginalLanguage() != null ? movie.getOriginalLanguage().toUpperCase() : "EN");
				if (StringUtils.isNotEmpty( movie.getPosterPath() )) {
					try {
						movie.setPosterPath( TheMovieDB.getInstance().getImageURL( movie.getPosterPath()) );
					} catch (MovieDbException | InterruptedException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			});
			return result.getResults().stream().collect( Collectors.toList() );
		}
		return null;
	}
	
	@PUT
	public Movie selectMovie(@QueryParam("id") long id, @QueryParam("movieDbId") int movieDbId, @QueryParam("language") Language language) throws MovieDbException, IOException, InterruptedException {
		MovieInfo movieDb = TheMovieDB.getInstance().getMovieInfo( movieDbId, language );
		return MovieManager.getInstance().associate(id, movieDb);
	}

}
