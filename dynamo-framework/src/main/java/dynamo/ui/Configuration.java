package dynamo.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

import dynamo.core.EventManager;
import dynamo.core.configuration.ConfigurationCategory;
import dynamo.core.configuration.items.AbstractConfigurationItem;
import dynamo.core.configuration.items.ConfigurationItemRow;
import dynamo.core.configuration.items.ImplementationListConfigurationItem;
import dynamo.core.configuration.items.ListConfigurationItem;
import dynamo.core.manager.ConfigAnnotationManager;
import dynamo.core.manager.ConfigurationManager;

@ManagedBean
@SessionScoped
public class Configuration extends DynamoManagedBean {

	private static final long serialVersionUID = 1L;
	
	private List<ConfigurationCategory> categories = null;
	
	public List<ConfigurationCategory> getCategories() {
		
		if (categories == null) {

			categories = new ArrayList<ConfigurationCategory>();
	
			for (AbstractConfigurationItem item : ConfigAnnotationManager.getInstance().getItems()) {
				
				String categoryName = item.getCategory();

				ConfigurationCategory category = null;
				for (ConfigurationCategory configurationCategory : categories) {
					if (configurationCategory.getName().equals( categoryName )) {
						category = configurationCategory;
						break;
					}
				}
				if ( category == null ) {
					category = new ConfigurationCategory( categoryName );
					categories.add( category );
				}
				category.addItem( item ); 

			}
		}

		return categories;
	}

	public void saveConfigurationItems() throws Exception {
		ConfigurationManager.getInstance().save();
		EventManager.getInstance().reportSuccess( "Settings saved successfully" );
	}

	public void addItem( String configKey ) {
		ListConfigurationItem item = (ListConfigurationItem) ConfigAnnotationManager.getInstance().getConfigurationItem( configKey  );
		try {
			item.add();
		} catch (InstantiationException | IllegalAccessException e) {
			reportException(e);
		}
	}

	public void removeItem( ActionEvent event ) throws ClassNotFoundException {
		
		String configKey = getParameter("key");
		int index = getIntegerParameter("index");

		ListConfigurationItem item = (ListConfigurationItem) ConfigAnnotationManager.getInstance().getConfigurationItem( configKey );
		Collection<ConfigurationItemRow> list = item.getList();

		for (Iterator<ConfigurationItemRow> iterator = list.iterator(); iterator.hasNext();) {
			ConfigurationItemRow row = iterator.next();
			if (row.getIndex() == index ) {
				iterator.remove();
			}
			if (row.getIndex() > index ) {
				row.setIndex( row.getIndex() - 1 );
			}
		}
	}
	
	public void addProvider( ImplementationListConfigurationItem item ) {
		item.addProvider();
	}
	
	public Object getConfig( String key ) throws ClassNotFoundException {
		AbstractConfigurationItem item = ConfigAnnotationManager.getInstance().getConfigurationItem(key);
		return item.getValue();
	}

}
