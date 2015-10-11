package dynamo.core.configuration.items;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;

public class ImplementationListConfigurationItem<E> extends ListConfigurationItem {
	
	private Set<E> allPossibleValues;

	public ImplementationListConfigurationItem( String key, Configurable configurable, Field field, Class configuredClass, boolean orderable ) {
		super( key, configurable, field, configuredClass, orderable );		
		this.contentsClass = configurable.contentsClass();
	}

	private E newProvider = null;
	
	public E getNewProvider() {
		return newProvider;
	}
	
	public void setNewProvider(E newProvider) {
		this.newProvider = newProvider;
	}
	
	public void addProvider() {
		addItem( newProvider );
	}

	@Override
	public Object getValueFromString(String value)
			throws ClassNotFoundException {
		if (list == null) {
			list = new ArrayList<ConfigurationItemRow>();
			if (value == null ) {
				StringBuffer valueStr = new StringBuffer();
				for (E provider : getAllowedValues()) {
					list.add( new ConfigurationItemRow(list.size(), provider) );
					valueStr.append( provider.getClass().getName() ).append(";");
					setStringValue( valueStr.toString() );
				}
			} else {
				String[] classNames = value.split(";");
				for (String className : classNames) {
					for (E provider : getAllowedValues()) {
						if ( provider.getClass().getName().equals( className) ) {
							list.add( new ConfigurationItemRow(list.size(), provider) );
							break;
						}
					}
				}
			}
		}

		return list;
	}

	public List<E> getAllowedValues() {
		if (allPossibleValues == null) {
			synchronized ( this ) {
				if (allPossibleValues == null) {
					try {
						allPossibleValues = new DynamoObjectFactory<E>("dynamo", contentsClass).getInstances();
					} catch (Exception e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			}
		}
		List<E> allowedValues = new ArrayList<E>();
		for (E object : allPossibleValues) {
			if (!(object instanceof Enableable) || ((Enableable)object).isEnabled()) {
				allowedValues.add(object);
			}
		}
		return allowedValues;
	}

	@Override
	public String toStringValue(Object value) {
		
		List<ConfigurationItemRow> rows = (List<ConfigurationItemRow>) value;
		
		StringBuffer valueStr = new StringBuffer();
		for (ConfigurationItemRow<E> row : rows) {
			E provider = row.getValue();
			valueStr.append( provider.getClass().getName() ).append(";");
		}
		return valueStr.toString();
	}
	
	@Override
	public void updateValue() {
		StringBuffer valueStr = new StringBuffer();
		int i = 0;
		for (Iterator<ConfigurationItemRow> iterator = getList().iterator(); iterator.hasNext();) {
			ConfigurationItemRow row = iterator.next();
			row.setIndex(i++);
			E provider = (E) row.getValue();
			valueStr.append( provider.getClass().getName() ).append(";");
		}
		setStringValue( valueStr.toString() );
	}
	
	public List<E> getRemainingProviders() {
		List<E> remainingProviders = new ArrayList<>();
		for (E provider : getAllowedValues()) {
			boolean found = false;
			for (ConfigurationItemRow row : getList()) {
				if (row.getValue() != null && row.getValue().toString().equals( provider.toString() )) {
					found = true;
					break;
				}
			}
			if (!found) {
				remainingProviders.add( provider );
			}
		}
		return remainingProviders;
	}
		
	public void addItem( E provider ) {
		getList().add( new ConfigurationItemRow(list.size(), provider) );
		updateValue();
	}

}
