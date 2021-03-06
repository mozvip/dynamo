package com.github.dynamo.core.services;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.dynamo.core.manager.ConfigAnnotationManager;
import com.github.dynamo.core.manager.ConfigurationManager;
import com.github.dynamo.core.model.Task;
import com.github.dynamo.core.model.TaskExecutor;

@Path("configuration")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigurationService {
	
	@GET
	@Path("/plugin-options")
	public List<PluginOption> getPluginOptions() {
		Map<Class<? extends Task>, List<Class<? extends TaskExecutor>>> pluginOptions = ConfigurationManager.getInstance().getPluginOptions();
		
		List<PluginOption> result = new ArrayList<>();
		for (Map.Entry<Class<? extends Task>, List<Class<? extends TaskExecutor>>> entry : pluginOptions.entrySet()) {
			if (entry.getValue() != null && entry.getValue().size() > 1) {
				Class<? extends Task> taskClass = entry.getKey();
				LabelledClass task = new LabelledClass( taskClass );
				List<LabelledClass> options = new ArrayList<>();
				for (Class<? extends TaskExecutor> executorClass : entry.getValue()) {
					options.add(new LabelledClass( executorClass ));
				}
				result.add( new PluginOption( task, options, ConfigurationManager.getInstance().getActivePlugin( taskClass ) ) );
			}
		}
		
		return result;
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
