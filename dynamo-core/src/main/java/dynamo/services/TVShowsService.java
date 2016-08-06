package dynamo.services;

import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.model.tvshows.TVShowManager;
import model.ManagedEpisode;
import model.ManagedSeries;

@Path("tvshows")
@Produces(MediaType.APPLICATION_JSON)
public class TVShowsService {
	
	@GET
	public List<ManagedSeries> getTVShows() {
		return TVShowManager.getInstance().getManagedSeries();
	}
	
	@GET
	@Path("/{id}")
	public ManagedSeries getTVShows(@PathParam("id") String id) {
		return TVShowManager.getInstance().getManagedSeries( id );
	}
	
	@GET
	@Path("/{id}/episodes")
	public List<ManagedEpisode> getEpisodes(@PathParam("id") String id) {
		return TVShowManager.getInstance().findEpisodes( TVShowManager.getInstance().getManagedSeries( id ) );
	}
	

	@javax.ws.rs.POST
	@Path("/add")
	public void addTVShow(@FormParam("id") String id) {
		// TVShowManager.getInstance().identifyFolder(path, id, language, audioLanguage, subtitlesLanguage);
		System.out.println( id );
	}
	
	@GET
	@Path("/folders")
	public List<java.nio.file.Path> getFolder() {
		return TVShowManager.getInstance().getFolders();
	}

}
