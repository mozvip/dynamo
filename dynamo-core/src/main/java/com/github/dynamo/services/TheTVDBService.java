package com.github.dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.github.dynamo.core.Language;
import com.github.dynamo.tvshows.model.TVShowManager;
import com.omertron.thetvdbapi.model.Series;

@Path("tvdb")
public class TheTVDBService {
	
	private static final TVShowManager tvShowManager = TVShowManager.getInstance();
	
	@GET
	@Path("search")
	public List<Series> getShows(@QueryParam("title") String searchTitle, @QueryParam("language") Language language) {
		return tvShowManager.searchSeries(searchTitle, language);
	}


}
