package dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.omertron.thetvdbapi.model.Series;

import dynamo.core.Language;
import dynamo.tvshows.model.TVShowManager;

@Path("tvdb")
public class TheTVDBService {
	
	private static final TVShowManager tvShowManager = TVShowManager.getInstance();
	
	@GET
	@Path("search")
	public List<Series> getShows(@QueryParam("title") String searchTitle, @QueryParam("language") Language language) {
		return tvShowManager.searchSeries(searchTitle, language);
	}


}
