package dynamo.core.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.core.configuration.items.AbstractConfigurationItem;
import dynamo.core.manager.ConfigAnnotationManager;

@Path("configuration")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigurationService {
	
	@GET
	@Path("categories")
	public List<String> getCategories() {
		List<AbstractConfigurationItem> items = ConfigAnnotationManager.getInstance().getItems();
		List<String> categories = new ArrayList<>();
		for (AbstractConfigurationItem item : items) {
			categories.add( item.getCategory() );
		}
		Collections.sort( categories );
		return categories;
	}

}
