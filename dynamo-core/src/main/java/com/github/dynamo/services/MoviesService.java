package com.github.dynamo.services;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.movies.model.Movie;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.movies.model.TheMovieDB;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

@Path("movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MoviesService {
	
	@POST
	@Path("/add")
	public void add( MovieRequest movieRequest ) throws MovieDbException, IOException, InterruptedException {
		MovieInfo movieInfo = TheMovieDB.getInstance().getMovieInfo(movieRequest.getMovieDbId(), null);
		Movie movie = MovieManager.getInstance().createMovieFromMovieDB( movieInfo, null, DownloadableStatus.WANTED, -1.0f, false);
		DownloadableManager.getInstance().want( movie );
	}

}
