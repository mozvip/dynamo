package dynamo.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.trakt.TraktManager;

@Path("trakt")
@Produces(MediaType.APPLICATION_JSON)
public class TraktService {
	
	@POST
	@Path("/auth/{code}")
	public boolean auth( @PathParam("code") String code ) {
		return TraktManager.getInstance().auth( code );
	}

}
