package dynamo.core.el;

import java.beans.FeatureDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.manager.ConfigValueManager;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;

public class DynamoELResolver extends ELResolver {
	
	private Map<String, Class<?>> shortNameClass = new HashMap<>();

	private DynamoELResolver() {
		Set<String> allTypes = DynamoObjectFactory.getReflections().getAllTypes();
		for (String className : allTypes) {
			Class<?> klass;
			try {
				klass = Class.forName(className);
				shortNameClass.put(klass.getSimpleName(), klass);
			} catch (ClassNotFoundException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
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

		if (base == null) {
			Class<?> klass = shortNameClass.get( (String) property );
			if (klass != null) {
				context.setPropertyResolved( true );
				return DynamoObjectFactory.getInstance( klass );
			}
		}
		
		String configKey = String.format("%s.%s", base.getClass().getSimpleName(), property);
		if (ConfigValueManager.getInstance().getConfigString( configKey ) != null) {
			context.setPropertyResolved(true);
			return ConfigValueManager.getInstance().getConfigString( configKey );
		}

		Method getterPropertyMethod = null;
		String capitalizedPropertyName = StringUtils.capitalize( (String) property );
		try {
			getterPropertyMethod = base.getClass().getMethod("get" + capitalizedPropertyName );
		} catch (NoSuchMethodException | SecurityException e) {
			try {
				getterPropertyMethod = base.getClass().getMethod("is" + capitalizedPropertyName );
			} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e1) {
			}
		}
		
		if (getterPropertyMethod != null) {
			try {
				Object value = getterPropertyMethod.invoke(base);
				context.setPropertyResolved(true);
				return value;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		}

		return null;
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
