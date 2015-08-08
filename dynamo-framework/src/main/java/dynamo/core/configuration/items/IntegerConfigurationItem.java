package dynamo.core.configuration.items;

import java.lang.reflect.Field;

import dynamo.core.configuration.Configurable;
import dynamo.core.manager.ErrorManager;

public class IntegerConfigurationItem extends AbstractConfigurationItem {

	public IntegerConfigurationItem( String key, Configurable configurable,
			Field field, Class configuredClass) {
		super( key, configurable, field, configuredClass );
	}

	@Override
	public Object getValueFromString(String value)
			throws ClassNotFoundException {
		if (value == null) {
			return null;
		}
		
		try {
			int val = Integer.parseInt( value );
			return val;
		} catch (java.lang.NumberFormatException e) {
			ErrorManager.getInstance().reportThrowable( e );
			return null;
		}
	}

	@Override
	public String toStringValue(Object value) {
		return value != null ? value.toString() : null;
	}

}
