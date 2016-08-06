package dynamo.core.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.DynamoApplication;
import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.configuration.items.AbstractConfigurationItem;
import dynamo.core.configuration.items.ListConfigurationItem;
import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.DaemonTask;
import dynamo.core.model.InitTask;
import dynamo.core.model.ServiceTask;
import dynamo.core.model.Task;
import dynamo.core.model.TaskExecutor;
import javassist.Modifier;

public class ConfigurationManager {
	
	private final static Logger LOGGER = LoggerFactory.getLogger( ConfigurationManager.class );
	public final static String DYNAMO_PACKAGE_PREFIX = "dynamo";
	
	private ConfigurationManager() {
		initPlugins();
	}

	static class SingletonHolder {
		static ConfigurationManager instance = new ConfigurationManager();
	}

	public static ConfigurationManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public void save() throws Exception {
		for (AbstractConfigurationItem configurationItem : ConfigAnnotationManager.getInstance().getItems()) {
			if (configurationItem instanceof ListConfigurationItem) {
				((ListConfigurationItem)configurationItem).updateValue();
			}
			ConfigValueManager.getInstance().setConfigString( configurationItem.getKey(), configurationItem.getStringValue() );
		}
		ConfigValueManager.getInstance().persistConfiguration();
		configureApplication();
	}
	
	private Map<Class<? extends Task>, Collection<Class<? extends TaskExecutor>>> pluginOptions = new HashMap<>();
	private Map<Class<? extends Task>, Class<? extends TaskExecutor>> activePlugins = new HashMap<>();

	public Map<Class<? extends Task>, Collection<Class<? extends TaskExecutor>>> getPluginOptions() {
		return pluginOptions;
	}

	public Map<Class<? extends Task>, Class<? extends TaskExecutor>> getActivePlugins() {
		return activePlugins;
	}

	public Class<? extends TaskExecutor> getActivePlugin(Class<? extends Task> klass) {
		return activePlugins.get( klass );
	}
	
	public void setActivePlugin(Class<? extends Task> task, Class<? extends TaskExecutor> executorClass) {
		activePlugins.put(task, executorClass);
		ConfigValueManager.getInstance().setConfigString("ConfigurationManager." + task.getName(), executorClass != null ? executorClass.getName() : "" );
	}
	
	public boolean isActive(Class<? extends TaskExecutor<?>> executorClass) {
		return( activePlugins.values().contains( executorClass ) );
	}

	private void initPlugins() {
		Reflections r = DynamoObjectFactory.getReflections();
		Set<Class<? extends TaskExecutor>> matchingClasses = r.getSubTypesOf( TaskExecutor.class );
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
		
		for (Entry<Class<? extends Task>, Collection<Class<? extends TaskExecutor>>> entry : pluginOptions.entrySet()) {
			Class<? extends Task> taskType = entry.getKey();
			Collection<Class<? extends TaskExecutor>> options = entry.getValue();

			String className = ConfigValueManager.getInstance().getConfigString( String.format("ConfigurationManager.%s", taskType.getName()) );

			Class<? extends TaskExecutor> activePlugin = null;
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
			Configurable annotation = field.getAnnotation( Configurable.class );

			String key = String.format( "%s.%s", instance.getClass().getSimpleName(), field.getName() );
			
			Object value = null;
			
			AbstractConfigurationItem configurationItem = ConfigAnnotationManager.getInstance().getConfigurationItem(key);
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
		
		return instance;
    }

	public static Object configureQueue(Class<? extends AbstractDynamoQueue> queueClass) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException {
		return getInstance().configureInstance( queueClass.newInstance() );
	}

	public void configureApplication() throws Exception {
		
		// first pass : set attribute values
		for (Class klass : ConfigAnnotationManager.getInstance().getConfiguredClasses()) {
			Object instance = null;
			try {
				instance = DynamoObjectFactory.getInstance(klass);
			} catch ( IllegalArgumentException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}

			if ( instance != null ) {
				LOGGER.debug("Configuring instance of class {}", klass.getCanonicalName());
				configureInstance( instance );
			}
		}
		
		// second pass : reconfigure
		for (Class klass : ConfigAnnotationManager.getInstance().getConfiguredClasses()) {
			Object instance = null;
			try {
				instance = DynamoObjectFactory.getInstance(klass);
			} catch (IllegalArgumentException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
			if (instance instanceof Reconfigurable) {
				if (!(instance instanceof Enableable) || ((Enableable) instance).isEnabled()) {
					((Reconfigurable)instance).reconfigure();
				}
			}
		}

		Set<InitTask> initTasks = new DynamoObjectFactory<>(DYNAMO_PACKAGE_PREFIX, InitTask.class).getInstances();
		for (InitTask initTask : initTasks) {
			if ( initTask.isEnabled() ) {
				BackLogProcessor.getInstance().schedule( initTask, false );
			} else {
				BackLogProcessor.getInstance().cancel( initTask );
			}
		}
		
		Set<ServiceTask> serviceTasks = new DynamoObjectFactory<>( DYNAMO_PACKAGE_PREFIX, ServiceTask.class ).getInstances();
		for (ServiceTask serviceTask : serviceTasks) {
			if ( serviceTask.isEnabled() ) {
				BackLogProcessor.getInstance().schedule( serviceTask, false );
			} else {
				BackLogProcessor.getInstance().cancel( serviceTask );
			}
		}

		Set<DaemonTask> daemonTasks = new DynamoObjectFactory<>( DYNAMO_PACKAGE_PREFIX, DaemonTask.class ).getInstances();
		for (DaemonTask daemonTask : daemonTasks) {
			if ( daemonTask.isEnabled() ) {
				BackLogProcessor.getInstance().schedule( daemonTask, false );
			} else {
				BackLogProcessor.getInstance().cancel( daemonTask );
			}
		}

	}

}
