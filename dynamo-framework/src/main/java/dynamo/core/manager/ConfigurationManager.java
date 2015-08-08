package dynamo.core.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.DynamoApplication;
import dynamo.core.Enableable;
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
import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.DaemonTask;
import dynamo.core.model.ServiceTask;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;

public class ConfigurationManager {
	
	public final static String DYNAMO_PACKAGE_PREFIX = "dynamo";
	private final static Logger logger = org.slf4j.LoggerFactory.getLogger( ConfigurationManager.class );

	private static DynamoObjectFactory<Object> discoverer;

	private ResourceBundle bundle;
	
	private ConfigurationManager() {
		
		try {
			loadConfiguration();
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}

		discoverer = new DynamoObjectFactory<Object>(DYNAMO_PACKAGE_PREFIX, (String) null);
		try {
			bundle = ResourceBundle.getBundle("labels");
		} catch (java.util.MissingResourceException e) {
			// doesn't matter
		}
	}

	static class SingletonHolder {
		static ConfigurationManager instance = new ConfigurationManager();
	}

	public static ConfigurationManager getInstance() {
		return SingletonHolder.instance;
	}

	private List<AbstractConfigurationItem> items = null;

	public List<AbstractConfigurationItem> getItems() {
		if (items == null) {
			synchronized ( ConfigurationManager.class ) {
				if ( items == null ) {
					items = new ArrayList<AbstractConfigurationItem>();
					Set<Class<?>> classes = discoverer.getMatchingClasses( false, false );

					List<Class<?>> configurableClasses = new ArrayList<>();
					for (Class<?> klass : classes) {
						if (extractAnnotations( klass, items, klass )) {
							configurableClasses.add( klass );
						}
					}

					initPlugins();

					for (Class<?> klass : configurableClasses) {
						try {
							DynamoObjectFactory.getInstance(klass);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | ClassNotFoundException | InvocationTargetException e) {
							ErrorManager.getInstance().reportThrowable( e );
						}			
					}
				}
			}
		}
		return items;
	}

	public AbstractConfigurationItem getConfigurationItem( String key ) {
		for (AbstractConfigurationItem item : getItems()) {
			if (item.getKey().equals( key )) {
				return item;
			}
		}
		logger.error(String.format("Could not find configuration item for %s", key));
		return null;
	}
	
	protected boolean extractAnnotations( Class<?> configuredClass, List<AbstractConfigurationItem> configItems, Class<?> currentClass ) {
		
		boolean configurable = false;

		if ( currentClass.getSuperclass() != null ) {
			// check super class
			configurable = extractAnnotations( configuredClass, configItems, currentClass.getSuperclass() );
		}

		Field[] fields = currentClass.getDeclaredFields();
		for (Field field : fields) {
			Configurable annotation = field.getAnnotation(Configurable.class);
			if (annotation != null) {
				
				configurable = true;

				String key = String.format( "%s.%s", configuredClass.getSimpleName(), field.getName() );
				
				AbstractConfigurationItem item = null;

				if (field.getType().isEnum()) {
					item = new SelectOneConfigurationItem( key, annotation, field, configuredClass, Arrays.asList( field.getType().getEnumConstants() ));
				} else if ( annotation.contentsClass() != null && annotation.contentsClass().isEnum() ) {
					item = new SelectManyConfigurationItem( key, annotation, field, configuredClass, Arrays.asList( annotation.contentsClass().getEnumConstants() ));
				} else if ( annotation.contentsClass() != null && annotation.contentsClass() != Path.class && Collection.class.isAssignableFrom( field.getType() ) && (annotation.contentsClass().isInterface() || Modifier.isAbstract( annotation.contentsClass().getModifiers() ))) {
				
					boolean orderable = List.class.isAssignableFrom( field.getType() );
					item = new ImplementationListConfigurationItem( key, annotation, field, configuredClass, orderable );
				
				} else if ( annotation.contentsClass() != null && Collection.class.isAssignableFrom( field.getType() ) ) {

					boolean orderable = List.class.isAssignableFrom( field.getType() );
					item = new ListConfigurationItem( key, annotation, field, configuredClass, orderable );

				} else if ( annotation.contentsClass() != null &&  ( Integer.class.equals( field.getType()) || int.class.equals( field.getType()))) {
					item = new IntegerConfigurationItem( key, annotation, field, configuredClass );
				} else if ( annotation.contentsClass() != null &&  ( Float.class.equals( field.getType()) || float.class.equals( field.getType()))) {
					item = new FloatConfigurationItem( key, annotation, field, configuredClass );
				} else if ( field.getType().equals(boolean.class) || field.getType().equals(Boolean.class) ) {
					item = new BooleanConfigurationItem( key, annotation, field, configuredClass);
				} else if (field.getType().equals( Path.class )){
					item = new PathConfigurationItem( key, annotation, field, configuredClass, annotation.folder());
				} else if ( StringUtils.isNotEmpty( annotation.allowedValues() ) ) {

					item = new SelectOneConfigurationItem( key, annotation, field, configuredClass, annotation.allowedValues());

				} else {
					item = new TextConfigurationItem( key, annotation, field, configuredClass );
				}

				configItems.add( item );
			}
		}
		
		return configurable;
	}

	public int getConfigInt(String key, int i) {
		String value = getConfigString(key, "" + i);
		return Integer.parseInt(value);
	}
	
	private ObjectMapper configurationMapper = new ObjectMapper();
	
	public synchronized void persistConfiguration() throws JsonGenerationException, JsonMappingException, IOException {
		configurationMapper.writeValue(new File("config.json"), configuration);
	}
	
	public synchronized void loadConfiguration() throws IOException {
		Path p = Paths.get("config.json");
		if (Files.isReadable( p )) {
			try (InputStream input = Files.newInputStream(p)) {
				configuration = configurationMapper.readValue(input, Map.class);
			}
		}
	}
	
	public void save() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JsonGenerationException, JsonMappingException, IOException {
		
		Set<Class> configuredClasses = new HashSet<>();
		
		for (AbstractConfigurationItem configurationItem : items) {
			if (configurationItem instanceof ListConfigurationItem) {
				((ListConfigurationItem)configurationItem).updateValue();
			}
			if (configurationItem.getConfiguredClass() != null) {
				configuredClasses.add( configurationItem.getConfiguredClass() );
			}
			
			setConfigString( configurationItem.getKey(), configurationItem.getStringValue() );
		}

		persistConfiguration();
		
		for (Class klass : configuredClasses) {
			Object instance = null;
			try {
				// if class is a singleton, reconfigure it
				Method method = klass.getMethod("getInstance", null);
				instance = method.invoke(klass, null);
			} catch (NoSuchMethodException e) {
				instance = DynamoObjectFactory.getInstance(klass);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}

			if ( instance != null ) {
				configureInstance( instance );
			}
		}
		
		Set<ServiceTask> serviceTasks = new DynamoObjectFactory<>(DynamoApplication.getInstance().getBasePackageName(), ServiceTask.class).getInstances();
		for (ServiceTask serviceTask : serviceTasks) {
			if ( serviceTask.isEnabled() ) {
				if (serviceTask instanceof Reconfigurable) {
					((Reconfigurable) serviceTask).reconfigure();
				}
				BackLogProcessor.getInstance().schedule( serviceTask, false );
			} else {
				BackLogProcessor.getInstance().cancel( serviceTask );
			}
		}

		for (DaemonTask daemonTask : DynamoApplication.getInstance().getDaemons()) {
			BackLogProcessor.getInstance().schedule( daemonTask );
		}
	}

	public String getConfigString(String key) {
		return getConfigString(key, null);
	}
	
	public void setConfigString(String key, String value) {
		if (mockedConfig) {
			mockedConfiguration.put(key, value);
		} else {
			configuration.put( key, value );
		}
	}

	public String getConfigString(String key, String defaultValue) {
		if (mockedConfig) {
			if (mockedConfiguration.containsKey( key )) {
				return mockedConfiguration.get(key).toString();
			}
			return null;
		}
		if (configuration.containsKey(key)) {
			return configuration.get(key);
		} else if ( defaultValue != null ) {
			configuration.put(key, defaultValue);
		}
		return defaultValue;
	}

	private static Map<String, Object> mockedConfiguration = new HashMap<>();
	private static Map<String, String> configuration = new HashMap<>();
	
	private static boolean mockedConfig = false;

	public static void mockConfiguration(String key, Object value) {
		mockedConfiguration.put( key, value );
		mockedConfig = true;
	}
	
	private Map<Class<? extends Task>, Collection<Class<? extends TaskExecutor<?>>>> pluginOptions = new HashMap<>();
	private Map<Class<? extends Task>, Class<? extends TaskExecutor<?>>> activePlugins = new HashMap<>();

	public Map<Class<? extends Task>, Collection<Class<? extends TaskExecutor<?>>>> getPluginOptions() {
		return pluginOptions;
	}

	public Map<Class<? extends Task>, Class<? extends TaskExecutor<?>>> getActivePlugins() {
		return activePlugins;
	}

	public Class<? extends TaskExecutor<?>> getActivePlugin(Class<? extends Task> klass) {
		return activePlugins.get( klass );
	}
	
	public void setActivePlugin(Class<? extends Task> task, Class<? extends TaskExecutor<?>> executorClass) {
		activePlugins.put(task, executorClass);
		setConfigString("ConfigurationManager." + task.getName(), executorClass != null ? executorClass.getName() : "" );
	}
	
	public boolean isActive(Class<? extends TaskExecutor<?>> executorClass) {
		return( activePlugins.values().contains( executorClass ) );
	}

	private void initPlugins() {
		DynamoObjectFactory<? extends TaskExecutor> executorDiscoverer = new DynamoObjectFactory(DYNAMO_PACKAGE_PREFIX, TaskExecutor.class);
		
		Set<?> matchingClasses = executorDiscoverer.getMatchingClasses(false, false);
		for (Object object : matchingClasses) {
			Class<? extends TaskExecutor<Task>> executorClass = (Class<? extends TaskExecutor<Task>>) object;
			Constructor[] constructors = executorClass.getConstructors();
			for (Constructor constructor : constructors) {
				Class[] types = constructor.getParameterTypes();
				if ( types.length > 0 && Task.class.isAssignableFrom( types[0] )) {
					Class<Task> taskType = types[0];
					
					if (!pluginOptions.containsKey( taskType)) {
						pluginOptions.put( taskType, new ArrayList<Class<? extends TaskExecutor<?>>>() );
					}
					pluginOptions.get( taskType ).add( executorClass );
				}
			}
		}
		
		for (Entry<Class<? extends Task>, Collection<Class<? extends TaskExecutor<?>>>> entry : pluginOptions.entrySet()) {
			Class<? extends Task> taskType = entry.getKey();
			Collection<Class<? extends TaskExecutor<?>>> options = entry.getValue();

			String className = getConfigString( String.format("ConfigurationManager.%s", taskType.getName()) );

			Class<? extends TaskExecutor<?>> activePlugin = null;
			if (StringUtils.isNotBlank(className)) {
				try {
					activePlugin = (Class<? extends TaskExecutor<?>>) Class.forName(className);
				} catch (ClassNotFoundException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
				if (activePlugin != null && !options.contains( activePlugin )) {
					ErrorManager.getInstance().reportError( "" );
					activePlugin = null;
				}
			}
			if (activePlugin == null && options != null && options.size() == 1) {
				activePlugin = options.iterator().next();
				setActivePlugin(taskType, activePlugin);
			}
			if (activePlugin != null) {
				activePlugins.put( taskType, activePlugin );
			}
		}
	}
		
	public TaskExecutor<Task> newExecutorInstance( Class<? extends TaskExecutor> klass, Object... constructorParams ) {
    	Class[] parameterTypes = new Class[ constructorParams.length ];
    	for (int i = 0; i < constructorParams.length; i++) {
			parameterTypes[i] = constructorParams[i].getClass();
		}
    	
    	Object[] params = null;

    	Constructor<? extends TaskExecutor<?>>[] constructors = (Constructor<? extends TaskExecutor<?>>[]) klass.getConstructors();
    	Constructor<? extends TaskExecutor<?>> constructorToUse = null;
    	for (Constructor<? extends TaskExecutor<?>> constructor : constructors) {
    		Class[] constructorParameterTypes = constructor.getParameterTypes();
    		if (Arrays.equals(parameterTypes, constructorParameterTypes)) {
    			// perfect match was found
    			constructorToUse = constructor;
    			params = constructorParams;
    			break;
    		}
		}
    	
    	if (constructorToUse == null) {
        	for (Constructor<? extends TaskExecutor<?>> constructor : constructors) {
        		Class[] constructorParameterTypes = constructor.getParameterTypes();
        		if (constructorParameterTypes.length > parameterTypes.length) {
        			// IMPROVE : check if the first parameter types are the same
        			params = Arrays.copyOf(constructorParams, constructorParameterTypes.length);
        			for(int i=constructorParams.length; i<params.length; i++) {
        				params[i] = DAOManager.getInstance().getDAO( constructorParameterTypes[i] );
        			}
        			constructorToUse = constructor;
        			break;
        		}
    		}
    	}

    	if (constructorToUse == null) {
    		return null;
    	}
    	try {
    		TaskExecutor instance = constructorToUse.newInstance( params );
			configureInstance( instance );
			return instance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
    	return null;		
	}

    public Object configureInstance( final Object instance ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
    	
    	getItems();
    	
    	List<Field> fields = new ArrayList<Field>();
    	Class currentClass = instance.getClass();
    	while( currentClass != null) {
    		fields.addAll( Arrays.asList( currentClass.getDeclaredFields() ));
    		currentClass = currentClass.getSuperclass();
    	}

		for (Field field : fields) {
			Configurable annotation = field.getAnnotation( Configurable.class );
			if (annotation != null) {

				String key = String.format( "%s.%s", instance.getClass().getSimpleName(), field.getName() );
				
				Object value = null;
				
				AbstractConfigurationItem configurationItem = getConfigurationItem(key);
				value = configurationItem != null ? configurationItem.getValue() : null;
				if (configurationItem != null && field.getType().isPrimitive() && value == null) { 
					Method getterMethod = null;
					try {
						getterMethod = instance.getClass().getMethod( "get" + StringUtils.capitalize( field.getName() ) );
					} catch (java.lang.IllegalArgumentException | NoSuchMethodException e) {
						try {
							getterMethod = instance.getClass().getMethod( "is" + StringUtils.capitalize( field.getName() ) );
						} catch (NoSuchMethodException | SecurityException e1) {
							ErrorManager.getInstance().reportThrowable( e );
						}
					}
					if (getterMethod != null) {
						configurationItem.setValue( getterMethod.invoke( instance ) );
					}
					continue;
				}
	
				String setterMethodName = "set" + StringUtils.capitalize( field.getName() );
				Method setterMethod = null;
				try {
					setterMethod = instance.getClass().getMethod( setterMethodName, field.getType() );
					setterMethod.invoke( instance, value );
				} catch (java.lang.IllegalArgumentException | NoSuchMethodException e) {
					ErrorManager.getInstance().reportError(
							String.format( "Unable to set configuration item %s on class %s : method %s was not found or is not correctly defined", annotation.name(), instance.getClass().getName(), setterMethodName)
						);
				}
			}
		}
		
		if (instance instanceof Reconfigurable) {
			if (!(instance instanceof Enableable) || ((Enableable) instance).isEnabled()) {
				new Thread( 
					new Runnable() {
						@Override
						public void run() {
							((Reconfigurable)instance).reconfigure();
						}
					}, String.format("Configuring instance of class %s", instance.getClass().getName())
				).start();
			}
		}

		return instance;
    }

	public static Object configureQueue(Class<? extends AbstractDynamoQueue> queueClass) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException {
		return getInstance().configureInstance( queueClass.newInstance() );
	}

	public String getLabel(String key) {
		if (bundle != null && bundle.containsKey(key)) {
			return bundle.getString(key);
		}
		return key;
	}

}
