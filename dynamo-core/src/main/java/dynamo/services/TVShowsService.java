package dynamo.services;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.omertron.thetvdbapi.TvDbException;

import dynamo.backlog.tasks.files.FileUtils;
import dynamo.core.manager.ErrorManager;
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
	@Consumes(MediaType.APPLICATION_JSON)
	public String addTVShow(TVShowRequest tvshow) {
		
		java.nio.file.Path parentFolder = FileUtils.getFolderWithMostUsableSpace( TVShowManager.getInstance().getFolders() );
		
		java.nio.file.Path tvShowFolder = parentFolder.resolve( tvshow.getSeriesName() );
		
		try {
			return TVShowManager.getInstance().identifyFolder(tvShowFolder, tvshow.getId(), TVShowManager.getInstance().getMetaDataLanguage().getShortName(), tvshow.getAudioLanguage(), tvshow.getSubtitlesLanguage());
		} catch (TvDbException | IOException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		
		return null;
	}
	
	@GET
	@Path("/folders")
	public List<java.nio.file.Path> getFolder() {
		return TVShowManager.getInstance().getFolders();
	}

}
