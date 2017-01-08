package com.github.dynamo.core.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.configuration.Configurable.DEFAULT;
import com.github.dynamo.core.model.Task;
import com.github.dynamo.core.services.ConfigurationItem;

public class ConfigAnnotationManager {
	public final static String DYNAMO_PACKAGE_PREFIX = "dynamo";
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ConfigAnnotationManager.class);

	private Set<Class<?>> configuredClasses = new HashSet<>();
	private Map<String, ConfigurationItem> items = new HashMap<>();
	private static Map<String, Object> mockedConfiguration = new HashMap<>();
	
	private static boolean mockedConfig = false;

	private ConfigAnnotationManager() {
		buildItemsList();
		try {
			Path p = Paths.get("config.json");
			if (Files.isReadable(p)) {
				try (InputStream input = Files.newInputStream(p)) {
					Map<String, String> configurationFromFile = configurationMapper.readValue(input, Map.class);
					for (Entry<String, String> entry : configurationFromFile.entrySet()) {
						setConfigString(entry.getKey(), entry.getValue());
					}
				}
			}
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
	}
	
	private ObjectMapper configurationMapper = new ObjectMapper();
	
	public synchronized void persistConfiguration() throws JsonGenerationException, JsonMappingException, IOException {
		Map<String, String> configuration = new HashMap<>();
		for (Entry<String, ConfigurationItem> entry : items.entrySet()) {
			configuration.put(entry.getKey(), entry.getValue().getValue());
		}
		configurationMapper.writeValue(new File("config.json"), configuration);
	}

	static class SingletonHolder {
		static ConfigAnnotationManager instance = new ConfigAnnotationManager();
	}
	
	public Map<String, ConfigurationItem> getItems() {
		return Collections.unmodifiableMap(items); 
	}

	public static ConfigAnnotationManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public void setConfigString( String key, String value ) {
		if (items.containsKey( key )) {
			items.get(key).setValue( value );
		} else {
			ErrorManager.getInstance().reportWarning( String.format("Configuration item %s was not found", key));
		}
	}
	
	public static void mockConfiguration(String key, Object value) {
		mockedConfiguration.put(key, value);
		mockedConfig = true;
	}	
	
	public String getConfigString(String key) {
		if (mockedConfig) {
			if (mockedConfiguration.containsKey(key)) {
				Object value = mockedConfiguration.get(key);
				return value != null ? value.toString() : null;
			}
		}
		if (items.containsKey(key)) {
			String value = items.get(key).getValue();
			if (value != null && value.equals("__NULL__")) {
				value = null;
			}
			return value;
		}
		return null;
	}	

	protected synchronized void buildItemsList() {
		Reflections reflections = DynamoObjectFactory.getReflections();
		
		Set<String> allTypes = reflections.getAllTypes();
		
		List<Class<?>> klasses = allTypes.stream()
			.map( typeName -> {
				try {
					return Class.forName( typeName );
				} catch (ClassNotFoundException e) {
					ErrorManager.getInstance().reportThrowable( e );
					return null;
				}
			})
		.filter( klass -> klass != null && ! ( Modifier.isAbstract(klass.getModifiers()) || klass.isInterface() ) )
		.collect( Collectors.toList());

		configuredClasses.addAll( 
			klasses.parallelStream()		
				.filter( klass -> extractAnnotations( klass, klass ))
				.collect( Collectors.toList())
		);
		
		Set<Class<? extends Task>> taskClasses = reflections.getSubTypesOf( Task.class );
		for (Class<? extends Task> taskClass : taskClasses) {
			
			String key = String.format("Plugin.%s", taskClass.getName());

			items.put(key, new ConfigurationItem( key, null, String.class, null, false, false, false, false ));
		}
		
	}
	
	public Set<Class<?>> getConfiguredClasses() {
		return configuredClasses;
	}

	private boolean extractAnnotations(Class<?> configuredClass, Class<?> currentClass) {
		
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

				String key = String.format("%s.%s", configuredClass.getSimpleName(), field.getName());
				
				boolean list = Collection.class.isAssignableFrom( field.getType() );
				boolean set = Set.class.isAssignableFrom( field.getType() );
				
				items.put(key, new ConfigurationItem( key, annotation.ifExpression(), !annotation.contentsClass().equals( DEFAULT.class ) ? annotation.contentsClass() : field.getType(), annotation.defaultValue(), annotation.required(), list, set, annotation.folder() ));
				
				configurable = true;
			}
		}
		
		if ( configurable ) {
			LOGGER.debug("Introspected configuration data from {}", currentClass.getName());
			configuredClasses.add( configuredClass );
		}

		return configurable;
	}

}
