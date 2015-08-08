package dynamo.core.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dynamo.core.configuration.items.AbstractConfigurationItem;

public class ConfigurationCategory implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<AbstractConfigurationItem> items = new ArrayList<AbstractConfigurationItem>();
	private String name;
	private boolean expanded = false;
	
	public ConfigurationCategory( String name ) {
		this.name = name;
	}
	
	public void addItem( AbstractConfigurationItem item ) {
		items.add( item );
	}
	
	public List<AbstractConfigurationItem> getItems() {
		return items;
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return name.replaceAll("[\\s/]", "");
	}
	
	public boolean isDisabled() {
		for (AbstractConfigurationItem abstractConfigurationItem : items) {
			if (!abstractConfigurationItem.isDisabled()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isExpanded() {
		for (AbstractConfigurationItem abstractConfigurationItem : items) {
			if (abstractConfigurationItem.hasError()) {
				expanded = true;
				break;
			}
		}
		return expanded;
	}
	
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	

}
