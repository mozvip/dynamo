package dynamo.core.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;

public class ConfigAnnotationManager {
	public final static String DYNAMO_PACKAGE_PREFIX = "dynamo";
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ConfigAnnotationManager.class);

	private Set<Class<?>> configuredClasses = new HashSet<>();;

	private ConfigAnnotationManager() {
		buildItemsList();
	}

	static class SingletonHolder {
		static ConfigAnnotationManager instance = new ConfigAnnotationManager();
	}

	public static ConfigAnnotationManager getInstance() {
		return SingletonHolder.instance;
	}

	protected synchronized void buildItemsList() {
		Reflections reflections = DynamoObjectFactory.getReflections();
		Set<Field> configurableFields = reflections.getFieldsAnnotatedWith( Configurable.class );
		
		Set<Class<?>> scannedClasses = new HashSet<>();
		
		for (Field field : configurableFields) {
			
			Class<?> declaringClass = field.getDeclaringClass();
			if (Modifier.isAbstract( declaringClass.getModifiers() ) || declaringClass.isInterface() ) {
				continue;
			}
			
			if (!scannedClasses.contains( declaringClass )) {
				if (extractAnnotations(declaringClass, declaringClass)) {
					configuredClasses.add( declaringClass );
				}
				scannedClasses.add( declaringClass );
			}
		}
	}
	
	public Set<Class<?>> getConfiguredClasses() {
		return configuredClasses;
	}

	private boolean extractAnnotations(Class<?> configuredClass, Class<?> currentClass) {
		
		LOGGER.info("Introspecting configuration data from {}", currentClass.getName());

		boolean configurable = Reconfigurable.class.isAssignableFrom( currentClass );

		Class<?> superclass = currentClass.getSuperclass();
		if (!superclass.equals(Object.class)) {
			// check super class
			boolean superClassConfigurable = extractAnnotations(configuredClass, superclass);
			configurable = configurable | superClassConfigurable;
		}

		Field[] fields = currentClass.getDeclaredFields();
		for (Field field : fields) {
			Configurable annotation = field.getAnnotation(Configurable.class);
			if (annotation != null) {
				configurable = true;
			}
		}
		
		if ( configurable ) {
			configuredClasses.add( configuredClass );
		}

		return configurable;
	}

}
