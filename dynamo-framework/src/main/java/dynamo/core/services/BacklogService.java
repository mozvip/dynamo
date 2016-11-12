package dynamo.core.services;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.TaskExecutor;

@Path("/backlog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BacklogService {
	
	@Path("/executors")
	@GET
	public List<TaskExecutor> getRunningExecutors() {
		return BackLogProcessor.getInstance().getRunningExecutors();
	}

}
