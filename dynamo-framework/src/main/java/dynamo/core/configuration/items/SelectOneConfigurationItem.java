package dynamo.core.configuration.items;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.configuration.Configurable;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;

public class SelectOneConfigurationItem extends AbstractConfigurationItem {
	
	private Collection allowedValues = null;
	private String allowedValuesExpression = null;

	public SelectOneConfigurationItem( String key, Configurable configurable,
			Field field, Class configuredClass, List allowedValues ) {
		super( key, configurable, field, configuredClass );
		this.allowedValues = allowedValues;
	}
	
	public SelectOneConfigurationItem( String key, Configurable configurable,
			Field field, Class configuredClass, String allowedValuesExpression ) {
		super( key, configurable, field, configuredClass );
		this.allowedValuesExpression = allowedValuesExpression;
	}	

	public Collection getAllowedValues() {
		if (allowedValues != null) {
			return allowedValues;
		}
		try {
			return (Collection) eval( DynamoObjectFactory.getInstance( getConfiguredClass() ), allowedValuesExpression, Collection.class );
		} catch (ClassNotFoundException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| InstantiationException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
		return null;
	}

	@Override
	public Object getValueFromString(String value) {
		if (StringUtils.isNotEmpty( value )) {
			if ( type.isEnum() ) {
				return Enum.valueOf(type, value);
			} else {
				return value;
			}
		}
		return null;
	}

	@Override
	public String toStringValue(Object value) {
		return value != null ? value.toString() : null;
	}




}
