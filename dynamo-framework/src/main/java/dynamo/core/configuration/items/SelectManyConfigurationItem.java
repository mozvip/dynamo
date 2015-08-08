package dynamo.core.configuration.items;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.configuration.Configurable;

public class SelectManyConfigurationItem extends SelectOneConfigurationItem {

	public SelectManyConfigurationItem( String key, Configurable configurable,
			Field field, Class configuredClass, List allowedValues) {
		super( key, configurable, field, configuredClass, allowedValues);
	}
	
	@Override
	public Object getValueFromString(String value) {
		
		if (value == null) {
			return null;
		}
		
		String[] values = value.split(";");
		List valuesList = new ArrayList();
		for (String str : values) {
			for (Object allowedValue : getAllowedValues()) {
				if (allowedValue.toString().equals( str )) {
					valuesList.add( allowedValue );
				}
			}
		}

		return valuesList;
	}
	
	@Override
	public String toStringValue(Object value) {
		if (value != null) {
			if (value instanceof Collection) {
				return StringUtils.join( (Collection)value, ";" );
			} else {
				return StringUtils.join( (Object[]) value, ";" );
			}
		}
		return null;
	}

}
