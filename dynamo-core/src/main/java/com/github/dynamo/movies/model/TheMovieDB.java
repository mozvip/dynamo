package com.github.dynamo.movies.model;

import java.util.concurrent.Semaphore;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.manager.ErrorManager;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.enumeration.SearchType;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;

public class TheMovieDB {
	
	private TheMovieDbApi api;
	private Semaphore theMovieDBSemaphore = new Semaphore(4);
	
	static class SingletonHolder {
		static TheMovieDB instance = new TheMovieDB();
	}
	
	public static TheMovieDB getInstance() {
		return SingletonHolder.instance;
	}
	
	private TheMovieDB() {
		try {
			api = new TheMovieDbApi("5a1a77e2eba8984804586122754f969f");
		} catch (MovieDbException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
	}

	public ResultList<MovieInfo> search( String name, int year, Language language ) throws MovieDbException, InterruptedException {
		try {
			theMovieDBSemaphore.acquire();
			return api.searchMovie( name, 0, language != null ? language.getShortName() : null, true, year > 0 ? year : 0, year > 0 ? year : 0, SearchType.PHRASE );
		} finally {
			theMovieDBSemaphore.release();
		}
	}
	
	public String getImageURL( String imagePath ) throws MovieDbException, InterruptedException {
		try {
			theMovieDBSemaphore.acquire();
			return api.createImageUrl(imagePath, "w185").toExternalForm();
		} finally {
			theMovieDBSemaphore.release();
		}
	}
	
	public MovieInfo getMovieInfo( int movieId, Language language ) throws MovieDbException, InterruptedException {
		try {
			theMovieDBSemaphore.acquire();
			return api.getMovieInfo( movieId, language != null ? language.getShortName() : null );	
		} finally {
			theMovieDBSemaphore.release();
		}
	}	

	public MovieInfo getMovieInfoImdb( String imdbId, Language language ) throws MovieDbException, InterruptedException {
		try {
			theMovieDBSemaphore.acquire();
			return api.getMovieInfoImdb( imdbId, language != null ? language.getShortName() : null );	
		} finally {
			theMovieDBSemaphore.release();
		}
	}	

}
