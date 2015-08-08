package dynamo.core.configuration.items;

import java.lang.reflect.Field;

import dynamo.core.configuration.Configurable;

public class BooleanConfigurationItem extends AbstractConfigurationItem {

	public BooleanConfigurationItem( String key, Configurable configurable,
			Field field, Class configuredClass) {
		super( key, configurable, field, configuredClass );
	}
	
	@Override
	public Object getValue() throws ClassNotFoundException {
		return isDisabled() ? false : super.getValue();
	}
	
	@Override
	public Object getValueFromString(String value) {
		return Boolean.valueOf( value );
	}
	
	@Override
	public String toStringValue(Object value) {
		return ((Boolean)value).toString();
	}

}
