package dynamo.core.services;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.AbstractDynamoQueue;

@Path("/backlog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BacklogService {
	
	@Path("/queues")
	@GET
	public Collection<AbstractDynamoQueue> getQueues() {
		return BackLogProcessor.getInstance().getQueues().values();
	}

}
