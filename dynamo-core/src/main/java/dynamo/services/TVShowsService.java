package dynamo.services;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.omertron.thetvdbapi.TvDbException;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.FileUtils;
import dynamo.backlog.tasks.tvshows.DeleteShowTask;
import dynamo.backlog.tasks.tvshows.ScanTVShowTask;
import dynamo.tvshows.model.ManagedEpisode;
import dynamo.tvshows.model.ManagedSeries;
import dynamo.tvshows.model.TVShowManager;
import dynamo.tvshows.model.UnrecognizedFile;
import dynamo.tvshows.model.UnrecognizedFolder;

@Path("tvshows")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
	
	@DELETE
	@Path("/{id}")
	public void deleteTVShow(@PathParam("id") String id) {
		BackLogProcessor.getInstance().runNow( new DeleteShowTask( TVShowManager.getInstance().getManagedSeries( id ), true ), true );
	}

	@GET
	@Path("/unrecognized")
	public List<UnrecognizedFolder> getUnrecognizedFolders() {
		return TVShowManager.getInstance().getUnrecognizedFolders();
	}
	
	@DELETE
	@Path("/unrecognized/{id}")
	public void deleteFile(@PathParam("id") long id) {
		TVShowManager.getInstance().deleteUnrecognizedFile( id );
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
	@Path("/toggleAutoDownload/{id}")
	public void toggleAutoDownload(@PathParam("id") String id) {
		TVShowManager.getInstance().toggleAutoDownload( id );
	}

	@POST
	@Path("/associate")
	public String associate( TVShowRequest request ) throws TvDbException, IOException {
		return TVShowManager.getInstance().identifyFolder( request.getFolder(), request.getTvdbId(), request.getMetadataLanguage(), request.getAudioLanguage(), request.getSubtitlesLanguage() );
	}
	
	@POST
	@Path("/save")
	public void save( ManagedSeries series ) {
		TVShowManager.getInstance().saveSeries( series);
	}
	

	@GET
	@Path("/{id}/episodes")
	public List<ManagedEpisode> getEpisodes(@PathParam("id") String id) {
		return TVShowManager.getInstance().findEpisodes( TVShowManager.getInstance().getManagedSeries( id ) );
	}

	@POST
	@Path("/add")
	public String addTVShow(TVShowRequest tvshow) throws TvDbException, IOException {
		
		java.nio.file.Path parentFolder = FileUtils.getFolderWithMostUsableSpace( TVShowManager.getInstance().getFolders() );
		
		java.nio.file.Path tvShowFolder = parentFolder.resolve( tvshow.getSeriesName() );
		
		return TVShowManager.getInstance().identifyFolder(tvShowFolder, tvshow.getTvdbId(), TVShowManager.getInstance().getMetaDataLanguage(), tvshow.getAudioLanguage(), tvshow.getSubtitlesLanguage());
	}
	
	@GET
	@Path("/folders")
	public List<java.nio.file.Path> getFolder() {
		return TVShowManager.getInstance().getFolders();
	}

}
