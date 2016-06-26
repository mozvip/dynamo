package dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import dynamo.model.Downloadable;

@Path("downloadable")
@Produces(MediaType.APPLICATION_JSON)
public class DownloadableService {
	
	@GET
	public List<Downloadable> get(@QueryParam("type") String type) {
		return null;
	}

}
