package dynamo.core.services;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dynamo.core.manager.ConfigAnnotationManager;
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
	public void saveItems(Map<String, String> items) throws Exception {
		for (Map.Entry<String, String> item : items.entrySet()) {
			ConfigAnnotationManager.getInstance().setConfigString( item.getKey(), item.getValue() );
		}
		ConfigurationManager.getInstance().save();
	}
	
	@GET
	@Path("/items")
	public Map<String, ConfigurationItem> getConfigurationItems() {
		return ConfigAnnotationManager.getInstance().getItems();
	}
	
	@POST
	@Path("/validFolder")
	public Response  validFolder( String folder ) {
		if (folder != null) {
			java.nio.file.Path path = Paths.get( folder );
			if (Files.isDirectory( path ) && Files.isReadable( path )) {
				return Response.ok().build();
			}
		}
		return Response.serverError().build()	;
	}

}
