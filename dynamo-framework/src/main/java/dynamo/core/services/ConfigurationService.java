package dynamo.core.services;

import java.util.Collection;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.core.manager.ConfigValueManager;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;

@Path("configuration")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigurationService {
	
	@GET
	@Path("/plugin-options")
	public Map<Class<? extends Task>, Collection<Class<? extends TaskExecutor>>> getPluginOptions() {
		return ConfigurationManager.getInstance().getPluginOptions();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void setParameter(ConfigItem item) throws Exception {
		ConfigValueManager.getInstance().setConfigString( item.getKey(), item.getValue() );
		ConfigurationManager.getInstance().save();
	}

}
