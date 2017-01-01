package dynamo.core.services;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.TaskSubmission;

@Path("/backlog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BacklogService {
	
	@Path("/submissions")
	@GET
	public Collection<TaskSubmission> getSubmissions() {
		return BackLogProcessor.getInstance().getSubmissions();
	}
	
	@Path("/cancel/{submissionId}")
	@POST
	public void cancel( @PathParam("submissionId") long submissionId ) {
		BackLogProcessor.getInstance().unschedule( submissionId );
	}

	@Path("/runNow/{submissionId}")
	@POST
	public void runNow( @PathParam("submissionId") long submissionId ) {
		BackLogProcessor.getInstance().runNow( submissionId );
	}

}
