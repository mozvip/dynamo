package dynamo.core.el;

import java.beans.FeatureDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.configuration.items.AbstractConfigurationItem;
import dynamo.core.manager.ConfigAnnotationManager;
import dynamo.core.manager.ConfigurationManager;

public class DynamoELResolver extends ELResolver {

	private DynamoELResolver() {
	}

	static class SingletonHolder {
		static DynamoELResolver instance = new DynamoELResolver();
	}

	public static DynamoELResolver getInstance() {
		return SingletonHolder.instance;
	}		

	@Override
	public Object getValue(ELContext context, Object base, Object property)
			throws NullPointerException, PropertyNotFoundException, ELException {

		Map<String, AbstractConfigurationItem> values = new HashMap<String, AbstractConfigurationItem>();

		if (base == null) {
			List<AbstractConfigurationItem> items = ConfigAnnotationManager.getInstance().getItems();
			for (AbstractConfigurationItem item : items) {
				if ( item.getPrefix().equals( property )) {
					String name = item.getKey().substring( item.getKey().indexOf('.') + 1);
					values.put( name, item );
				}
			}
		}

		if ( base instanceof Map ) {

			Map<String, AbstractConfigurationItem> map = ((Map<String, AbstractConfigurationItem>) base);
			
			AbstractConfigurationItem item = map.get( property );
			
			if (item != null ) {
				context.setPropertyResolved( true );
				try {
					return item.getValue();
				} catch (ClassNotFoundException e) {
					throw new ELException( e );
				}				
			} else {
				
				for (Map.Entry<String, AbstractConfigurationItem> entry : map.entrySet()) {
					if (entry.getKey().startsWith( (String) property )) {
						values.put( entry.getKey().substring( entry.getKey().indexOf('.') + 1), entry.getValue());
					}
				}
				
			}
			
		} else if ( base != null ){
			
			Method propertyGetterMethod = null;
			String capitalizedProperty = StringUtils.capitalize(property.toString());

			try {
				propertyGetterMethod = base.getClass().getMethod("get"+capitalizedProperty);
			} catch (NoSuchMethodException e) {
				try {
					propertyGetterMethod = base.getClass().getMethod("is"+capitalizedProperty);
				} catch (NoSuchMethodException e1) {
				}
			}

			// default case
			if (propertyGetterMethod != null) {
				Object value;
				try {
					value = propertyGetterMethod.invoke( base );
					context.setPropertyResolved(true);
					return value;
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new ELException( e );
				}
			}
			
		}

		if (values.isEmpty()) {
			return null;
		}
		
		context.setPropertyResolved(true);

		return values;
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property)
			throws NullPointerException, PropertyNotFoundException, ELException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property,
			Object value) throws NullPointerException,
			PropertyNotFoundException, PropertyNotWritableException,
			ELException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property)
			throws NullPointerException, PropertyNotFoundException, ELException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
			Object base) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		// TODO Auto-generated method stub
		return null;
	}

}
