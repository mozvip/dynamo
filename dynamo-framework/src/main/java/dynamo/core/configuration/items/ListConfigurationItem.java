package dynamo.core.configuration.items;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.configuration.Configurable;
import dynamo.core.manager.ErrorManager;

public class ListConfigurationItem extends AbstractConfigurationItem {

	private boolean orderable;
	
	protected Class contentsClass = null;

	public ListConfigurationItem( String key, Configurable configurable, Field field, Class configuredClass, boolean orderable ) {
		super( key, configurable, field, configuredClass );
		this.contentsClass = configurable.contentsClass();
		this.orderable = orderable;
	}
	
	public boolean isOrderable() {
		return orderable;
	}

	protected Collection<ConfigurationItemRow> list = null;
	
	private Collection createList() {
		if (orderable) {
			list = new ArrayList<ConfigurationItemRow>();
		} else {
			list = new HashSet<ConfigurationItemRow>();	
		}
		return list;
	}

	@Override
	public Object getValueFromString(String value)
			throws ClassNotFoundException {
		list = createList();

		if (StringUtils.isNotBlank( value )) {
			String[] values = value.split(";");
			for (String string : values) {
				
				if (StringUtils.isBlank(string)) {
					continue;
				}
	
				Object item = string;
				if (contentsClass.equals( Path.class )) {
					item = Paths.get( string );
				} else {
					// TODO : convert to required type				
				}
	
				list.add( new ConfigurationItemRow( list.size(), item ) );
			}
		}
		return list;
	}

	public void updateValue() {
		StringBuffer valueStr = new StringBuffer();
		int i = 0;
		for (Iterator<ConfigurationItemRow> iterator = getList().iterator(); iterator.hasNext();) {
			ConfigurationItemRow row = iterator.next();
			row.setIndex(i++);
			valueStr.append( row.getValue().toString() ).append(";");
		}
		setStringValue( valueStr.toString() );
	}	

	@Override
	public String toStringValue(Object value) {
		List<ConfigurationItemRow> rows = (List<ConfigurationItemRow>) value;
		StringBuffer valueStr = new StringBuffer();
		for (ConfigurationItemRow row : rows) {
			Object rowValue = row.getValue();
			valueStr.append( row.getValue().toString() ).append(";");
		}
		return valueStr.toString();
	}

	public Class getContentsClass() {
		return contentsClass;
	}

	public void add() throws InstantiationException, IllegalAccessException {
		if (list == null) {
			list = createList();
		}
		Object instance;

		if ( getContentsClass().equals( Path.class )) {
			instance = Paths.get("/");
		} else {
			instance = getContentsClass().newInstance();
		}
		list.add( new ConfigurationItemRow(list.size(), instance) );
	}
	
	public Collection<ConfigurationItemRow> getList() {
		if (list == null) {
			try {
				list = (Collection<ConfigurationItemRow>) getValueFromString( getStringValue() );
			} catch (ClassNotFoundException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
		return list;		
	}	
	
	@Override
	public Object getValue() throws ClassNotFoundException {
		Collection value = null;
		try {
			value = getList().getClass().newInstance();
			for (ConfigurationItemRow row : getList()) {
				value.add( row.getValue() );
			}
		} catch (InstantiationException | IllegalAccessException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		return value;
	}

	public void moveUp( int index ) throws ClassNotFoundException {
		List<ConfigurationItemRow> l = (List<ConfigurationItemRow>) getList();
		l.add( index-1, l.remove( index ));
		updateValue();
	}
	
	public void moveDown( int index ) throws ClassNotFoundException {
		List<ConfigurationItemRow> l = (List<ConfigurationItemRow>) getList();
		l.add( index+1, l.remove( index ));
		updateValue();
	}
	
	public void removeItem( Object object ) throws ClassNotFoundException {
		getList().remove( object );
		updateValue();
	}	

}
