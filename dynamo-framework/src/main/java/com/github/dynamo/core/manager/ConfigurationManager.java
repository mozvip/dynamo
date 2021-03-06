package com.github.dynamo.core.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.Enableable;
import com.github.dynamo.core.EventManager;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.model.DaemonTask;
import com.github.dynamo.core.model.InitTask;
import com.github.dynamo.core.model.ServiceTask;
import com.github.dynamo.core.model.Task;
import com.github.dynamo.core.model.TaskExecutor;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javassist.Modifier;

public class ConfigurationManager {
	
	private final static Logger LOGGER = LoggerFactory.getLogger( ConfigurationManager.class );
	
	private ConfigurationManager() {
	}

	static class SingletonHolder {
		static ConfigurationManager instance = new ConfigurationManager();
	}

	public static ConfigurationManager getInstance() {
		return SingletonHolder.instance;
	}

	public void save() throws Exception {
		ConfigAnnotationManager.getInstance().persistConfiguration();
		EventManager.getInstance().reportSuccess("Configuration Saved");
		configureApplication();
	}
	
	private Map<Class<? extends Task>, List<Class<? extends TaskExecutor>>> pluginOptions = new HashMap<>();
	private Map<Class<? extends Task>, Class<? extends TaskExecutor>> activePlugins = new HashMap<>();

	public Map<Class<? extends Task>, List<Class<? extends TaskExecutor>>> getPluginOptions() {
		return pluginOptions;
	}

	public Map<Class<? extends Task>, Class<? extends TaskExecutor>> getActivePlugins() {
		return activePlugins;
	}

	public Class<? extends TaskExecutor> getActivePlugin(Class<? extends Task> klass) {
		String key = String.format("Plugin.%s", klass.getName());
		String configuredPlugin = ConfigAnnotationManager.getInstance().getConfigString(key);
		if (configuredPlugin != null) {
			try {
				return (Class<? extends TaskExecutor>) Class.forName( configuredPlugin );
			} catch (ClassNotFoundException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}
		return activePlugins.get( klass );
	}
	
	public boolean isActive(Class<? extends TaskExecutor<?>> executorClass) {
		return( activePlugins.values().contains( executorClass ) );
	}

	private void initPlugins() {
		Set<Class<? extends TaskExecutor>> matchingClasses = DynamoObjectFactory.getReflections().getSubTypesOf( TaskExecutor.class );
		
		for (Class<? extends TaskExecutor> executorClass : matchingClasses) {
			if (executorClass.isInterface() || Modifier.isAbstract( executorClass.getModifiers() )) {
				continue;
			}
			Constructor[] constructors = executorClass.getConstructors();
			for (Constructor constructor : constructors) {
				Class[] types = constructor.getParameterTypes();
				if ( types.length > 0 && Task.class.isAssignableFrom( types[0] )) {
					Class<Task> taskType = types[0];
					
					if (!pluginOptions.containsKey( taskType)) {
						pluginOptions.put( taskType, new ArrayList<Class<? extends TaskExecutor>>() );
					}
					pluginOptions.get( taskType ).add( executorClass );
				}
			}
		}
		
		for (Entry<Class<? extends Task>, List<Class<? extends TaskExecutor>>> entry : pluginOptions.entrySet()) {
			Class<? extends Task> taskType = entry.getKey();
			List<Class<? extends TaskExecutor>> options = entry.getValue();

			String className = ConfigAnnotationManager.getInstance().getConfigString( String.format("Plugin.%s", taskType.getName()) );

			Class<? extends TaskExecutor> activePlugin = null;
			if (StringUtils.isNotBlank(className)) {
				try {
					activePlugin = (Class<? extends TaskExecutor<?>>) Class.forName(className);
				} catch (ClassNotFoundException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
				if (activePlugin != null && !options.contains( activePlugin )) {
					ErrorManager.getInstance().reportError( null, String.format("%s is not a valid plugin for %s", className, taskType.getName()) );
					activePlugin = null;
				}
			}
			activePlugins.put( taskType, activePlugin != null ? activePlugin : options.get( 0 ) );
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
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
    	return null;		
	}
	
	private Object toValue( Class fieldType, Configurable annotation, String stringValue ) {
		Object value = stringValue;
		
		if (stringValue != null) {
			if (fieldType.equals( boolean.class )) {
				value = Boolean.valueOf( stringValue );
			} else if (fieldType.equals( int.class )) {
				value = Integer.valueOf( stringValue );
			} else if (fieldType.equals( float.class )) {
				value = Float.valueOf( stringValue );
			} else if (fieldType.equals( Path.class )) {
				value = Paths.get( stringValue );
			} else if (fieldType.isEnum()) {
				value = !stringValue.equals("") ? Enum.valueOf(fieldType, stringValue) : null;
			} else if (Collection.class.isAssignableFrom( fieldType )) {
				String[] values = stringValue.split(";");
				Class contentsClass = annotation.contentsClass();
				if (Set.class.isAssignableFrom( fieldType )) {
					value = new HashSet();
				} else {
					value = new ArrayList();
				}
				int i = 0;
				for (String string : values) {
					if (StringUtils.isNotBlank( string )) {
						((Collection)value).add( toValue( contentsClass, null, string ) );
					}
				}
			} else if (!fieldType.equals( String.class )) {
				try {
					value = DynamoObjectFactory.getInstance( Class.forName( stringValue ));
				} catch (ClassNotFoundException e) {
					LOGGER.warn("Class {} referenced in configuration could not be found", stringValue);
				}
			}
		}
		
		return value;
	}
	
	public void configureField( final Object instance, Field field, String configurationValue ) {
		if (field.getType().isPrimitive() && configurationValue == null) {
			return ;
		}

		Configurable annotation = field.getAnnotation( Configurable.class );

		Object value = toValue( field.getType(), annotation, configurationValue );

		String setterMethodName = "set" + StringUtils.capitalize( field.getName() );
		Method setterMethod = null;
		try {
			setterMethod = instance.getClass().getMethod( setterMethodName, field.getType() );
			setterMethod.invoke( instance, value );
		} catch (java.lang.IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			ErrorManager.getInstance().reportThrowable(String.format("Error Configuring field %s.%s, value = '%s'", instance.getClass().getName(), field.getName(), configurationValue), e);
		}
	}

    public Object configureInstance( final Object instance ) {

    	List<Field> fields = new ArrayList<Field>();
    	Class currentClass = instance.getClass();
    	while( currentClass != null) {
    		for (Field field : currentClass.getDeclaredFields()) {
    			Configurable annotation = field.getAnnotation( Configurable.class );
    			if (annotation != null) {
    				fields.add( field );
    			}
			}
    		currentClass = currentClass.getSuperclass();
    	}

		for (Field field : fields) {
			String key = String.format( "%s.%s", instance.getClass().getSimpleName(), field.getName() );
			String configurationValue = ConfigAnnotationManager.getInstance().getConfigString(key);
			configureField(instance, field, configurationValue);
		}
		
		return instance;
    }

	public void configureApplication() throws Exception {
		
		initPlugins();
		
		// get all instances to configure
		List<?> instances = ConfigAnnotationManager.getInstance().getConfiguredClasses().parallelStream().map( klass -> DynamoObjectFactory.getInstance(klass)).filter(instance -> instance != null).collect( Collectors.toList() );
		
		// first pass : set attribute values
		instances.parallelStream()
			.forEach( instance -> configureInstance( instance ));
		
		// second pass : reconfigure
		List<?> runnables = instances.parallelStream()
			.filter(instance -> instance instanceof Reconfigurable)
			.filter(instance -> !(instance instanceof Enableable) || ((Enableable) instance).isEnabled())
			.map( instance -> 
				new Runnable() {
					@Override
					public void run() {
						((Reconfigurable) instance).reconfigure();
					}
			}).collect( Collectors.toList() );

		for (Object runnable : runnables) {
			new Thread( (Runnable)runnable ).start();
		}


		Set<InitTask> initTasks = (Set<InitTask>) DynamoObjectFactory.getInstances( InitTask.class );
		for (InitTask initTask : initTasks) {
			if ( initTask.isEnabled() ) {
				BackLogProcessor.getInstance().schedule( initTask, false );
			} else {
				BackLogProcessor.getInstance().unschedule( initTask.getClass() );
			}
		}

		Set<ServiceTask> serviceTasks = (Set<ServiceTask>) DynamoObjectFactory.getInstances( ServiceTask.class );
		for (ServiceTask serviceTask : serviceTasks) {
			if ( serviceTask.isEnabled() ) {
				BackLogProcessor.getInstance().schedule( serviceTask, false );
			} else {
				BackLogProcessor.getInstance().unschedule( serviceTask.getClass() );
			}
		}

		Set<DaemonTask> daemonTasks = (Set<DaemonTask>) DynamoObjectFactory.getInstances( DaemonTask.class );
		for (DaemonTask daemonTask : daemonTasks) {
			if ( daemonTask.isEnabled() ) {
				BackLogProcessor.getInstance().schedule( daemonTask, false );
			} else {
				BackLogProcessor.getInstance().unschedule( daemonTask.getClass() );
			}
		}

		registerListeners();
	}

	private void registerListeners() throws InstantiationException, IllegalAccessException {
		EventBus eventBus = BackLogProcessor.getInstance().newEventBus();

		Set<Method> subscribeMethods = DynamoObjectFactory.getReflections().getMethodsAnnotatedWith(Subscribe.class);
		Set<Class> klasses = new HashSet<>();
		for (Method method : subscribeMethods) {
			klasses.add( method.getDeclaringClass() );
		}
		for (Class klass : klasses) {
			Object eventListener = klass.newInstance();
			eventBus.register( eventListener );
		}
	}

}
