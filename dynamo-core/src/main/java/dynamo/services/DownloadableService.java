package dynamo.services;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.core.model.DownloableCount;
import dynamo.core.model.DownloadableDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;

@Path("downloadable")
@Produces(MediaType.APPLICATION_JSON)
public class DownloadableService {

	@GET
	public List<Downloadable> get(@QueryParam("type") String type, @QueryParam("status") String allStatus) {
		Class<? extends Downloadable> klass = DownloadableManager.getInstance().getDownloadableTypeBySimpleName( type );
		
		DownloadableDAO daoInstance = DownloadableManager.getInstance().getDAOInstance( klass );
		String[] statuses = allStatus.split(",");
		List<Downloadable> downloads = new ArrayList<>();
		for (String status : statuses) {
			downloads.addAll( daoInstance.findByStatus( DownloadableStatus.valueOf( status ) ) );
		}
		Collections.sort(downloads, (Downloadable d1, Downloadable d2) -> d1.getName().compareTo(d2.getName()));
		return downloads;
	}
	
	@DELETE
	@Path("{id}")
	public void delete(@PathParam("id") long id) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		BackLogProcessor.getInstance().schedule( new DeleteDownloadableTask( id ));
	}
	
	@POST
	@Path("/redownload/{id}")
	public void redownload(@PathParam("id") long id) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException  {
		DownloadableManager.getInstance().redownload(id);
	}
	
	@POST
	@Path("/want/{id}")
	public void want(@PathParam("id") long id) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException  {
		DownloadableManager.getInstance().want(id);
	}

	@GET
	@Path("/counts")
	public List<DownloableCount> getCount() {
		return DownloadableManager.getInstance().getCounts();
	}

}
