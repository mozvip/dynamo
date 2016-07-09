package dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadInfo;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;

@Path("downloadable")
@Produces(MediaType.APPLICATION_JSON)
public class DownloadableService {

	@GET
	public List<DownloadInfo> get(@QueryParam("type") String type, @QueryParam("status") String status) {
		Class<? extends Downloadable> klass = DownloadableManager.getInstance().getDownloadableTypeBySimpleName( type );
		return DownloadableManager.getInstance().findByStatus( klass, DownloadableStatus.valueOf( status ) );
	}

}
