package dynamo.services;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.omertron.thetvdbapi.TvDbException;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.FileUtils;
import dynamo.core.manager.ErrorManager;
import dynamo.model.tvshows.TVShowManager;
import model.ManagedEpisode;
import model.ManagedSeries;
import model.UnrecognizedFile;
import model.UnrecognizedFolder;
import model.backlog.ScanTVShowTask;

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
	@Path("/unrecognized")
	public List<UnrecognizedFolder> getUnrecognizedFolders() {
		return TVShowManager.getInstance().getUnrecognizedFolders();
	}
	
	@GET
	@Path("/{id}/unrecognized")
	public List<UnrecognizedFile> getUnrecognizedFiles(@PathParam("id") String id) {
		return TVShowManager.getInstance().getUnrecognizedFiles(id);
	}

	@POST
	@Path("/rescan/{id}")
	public void rescan(@PathParam("id") String id) {
		BackLogProcessor.getInstance().runNow( new ScanTVShowTask( TVShowManager.getInstance().getManagedSeries( id ) ), true );
	}
	
	@POST
	@Path("/save/{id}")
	public void save(@PathParam("id") String id) {
		// TODO
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
