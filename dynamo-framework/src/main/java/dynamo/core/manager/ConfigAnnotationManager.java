package dynamo.core.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.configuration.items.AbstractConfigurationItem;
import dynamo.core.configuration.items.BooleanConfigurationItem;
import dynamo.core.configuration.items.FloatConfigurationItem;
import dynamo.core.configuration.items.ImplementationListConfigurationItem;
import dynamo.core.configuration.items.IntegerConfigurationItem;
import dynamo.core.configuration.items.ListConfigurationItem;
import dynamo.core.configuration.items.PathConfigurationItem;
import dynamo.core.configuration.items.SelectManyConfigurationItem;
import dynamo.core.configuration.items.SelectOneConfigurationItem;
import dynamo.core.configuration.items.TextConfigurationItem;

public class ConfigAnnotationManager {
	public final static String DYNAMO_PACKAGE_PREFIX = "dynamo";
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ConfigAnnotationManager.class);

	private List<AbstractConfigurationItem> items = null;
	private Map<String, AbstractConfigurationItem> itemMap = new HashMap<>();
	private Set<Class<?>> configuredClasses = new HashSet<>();

	private ConfigAnnotationManager() {
		buildItemsList();
	}

	static class SingletonHolder {
		static ConfigAnnotationManager instance = new ConfigAnnotationManager();
	}

	public static ConfigAnnotationManager getInstance() {
		return SingletonHolder.instance;
	}

	public synchronized List<AbstractConfigurationItem> getItems() {
		if (items == null) {
			buildItemsList();
		}
		return items;
	}

	protected synchronized void buildItemsList() {
		items = new ArrayList<AbstractConfigurationItem>();
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

	public AbstractConfigurationItem getConfigurationItem(String key) {
		return itemMap.get(key);
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

				String key = String.format("%s.%s", configuredClass.getSimpleName(), field.getName());

				AbstractConfigurationItem item = null;

				if (field.getType().isEnum()) {
					item = new SelectOneConfigurationItem(key, annotation, field, configuredClass, Arrays.asList(field.getType().getEnumConstants()));
				} else if (annotation.contentsClass() != null && annotation.contentsClass().isEnum()) {
					item = new SelectManyConfigurationItem(key, annotation, field, configuredClass,
							Arrays.asList(annotation.contentsClass().getEnumConstants()));
				} else if (annotation.contentsClass() != null && annotation.contentsClass() != Path.class && Collection.class.isAssignableFrom(field.getType())
						&& (annotation.contentsClass().isInterface() || Modifier.isAbstract(annotation.contentsClass().getModifiers()))) {

					boolean orderable = List.class.isAssignableFrom(field.getType());
					item = new ImplementationListConfigurationItem(key, annotation, field, configuredClass, orderable);

				} else if (annotation.contentsClass() != null && Collection.class.isAssignableFrom(field.getType())) {

					boolean orderable = List.class.isAssignableFrom(field.getType());
					item = new ListConfigurationItem(key, annotation, field, configuredClass, orderable);

				} else if (annotation.contentsClass() != null && (Integer.class.equals(field.getType()) || int.class.equals(field.getType()))) {
					item = new IntegerConfigurationItem(key, annotation, field, configuredClass);
				} else if (annotation.contentsClass() != null && (Float.class.equals(field.getType()) || float.class.equals(field.getType()))) {
					item = new FloatConfigurationItem(key, annotation, field, configuredClass);
				} else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
					item = new BooleanConfigurationItem(key, annotation, field, configuredClass);
				} else if (field.getType().equals(Path.class)) {
					item = new PathConfigurationItem(key, annotation, field, configuredClass, annotation.folder());
				} else if (StringUtils.isNotEmpty(annotation.allowedValues())) {

					item = new SelectOneConfigurationItem(key, annotation, field, configuredClass, annotation.allowedValues());

				} else {
					item = new TextConfigurationItem(key, annotation, field, configuredClass);
				}

				items.add(item);
				itemMap.put( key, item );
			}
		}
		
		if ( configurable ) {
			configuredClasses.add( configuredClass );
		}

		return configurable;
	}

}
