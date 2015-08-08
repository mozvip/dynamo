package dynamo.core.configuration.items;

import java.lang.reflect.Field;

import dynamo.core.configuration.Configurable;
import dynamo.core.manager.ErrorManager;

public class FloatConfigurationItem extends AbstractConfigurationItem {

	public FloatConfigurationItem( String key, Configurable configurable,
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
			float val = Float.parseFloat( value );
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
