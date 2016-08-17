package dynamo.core.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.core.configuration.items.AbstractConfigurationItem;
import dynamo.core.manager.ConfigAnnotationManager;
import dynamo.core.manager.ConfigValueManager;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;

@Path("configuration")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigurationService {
	
	@GET
	public Map<String, List<AbstractConfigurationItem>> getItems() {
		List<AbstractConfigurationItem> items = ConfigAnnotationManager.getInstance().getItems();
		Map<String, List<AbstractConfigurationItem>> categories = new HashMap<>();
		for (AbstractConfigurationItem item : items) {
			List<AbstractConfigurationItem> category;
			if (!categories.containsKey( item.getCategory() )) {
				category = new ArrayList<>();
				categories.put( item.getCategory(), category );	
			} else {
				category = categories.get( item.getCategory() );	
			}
			category.add( item );
		}
		return categories;
	}
	
	@GET
	@Path("/plugin-options")
	public Map<Class<? extends Task>, Collection<Class<? extends TaskExecutor>>> getPluginOptions() {
		return ConfigurationManager.getInstance().getPluginOptions();
	}
	
	@POST
	public void setParameter(ConfigItem item) {
		ConfigValueManager.getInstance().setConfigString( item.getKey(), item.getValue() );
	}

}
