package dynamo.core.configuration.items;

import java.lang.reflect.Field;

import dynamo.core.configuration.Configurable;

public class TextConfigurationItem extends AbstractConfigurationItem {

	public TextConfigurationItem( String key, Configurable configurable,
			Field field, Class configuredClass) {
		super( key, configurable, field, configuredClass );
	}

	@Override
	public Object getValueFromString(String value)
			throws ClassNotFoundException {
		return value;
	}

	@Override
	public String toStringValue(Object value) {
		return (String)value;
	}

}
