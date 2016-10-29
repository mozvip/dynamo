package dynamo.core.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import dynamo.core.configuration.ClassDescription;

public class DynamoObjectFactory {
	
	private static Reflections reflections = new Reflections("dynamo", new FieldAnnotationsScanner(), new SubTypesScanner( false ), new TypeAnnotationsScanner());
	
	private static final String NO_INSTANCE_MARKER = "NO_INSTANCE";
	
	private static LoadingCache<Class<?>, Object> instancesCache = CacheBuilder.newBuilder().
			build(new CacheLoader<Class<?>, Object>() {
				public Object load(Class<?> klass) throws Exception {
					Object instance = null;
	    			try {
	    				Method singletonGetInstance = klass.getMethod("getInstance");
	    				if (Modifier.isStatic(singletonGetInstance.getModifiers())) {
	    					instance = singletonGetInstance.invoke( null );
	    				}
	    			} catch (NoSuchMethodException | SecurityException e) {
	    			}
	    			
	    			if (instance == null) {
	    				try {
							instance = klass.newInstance();
						} catch (java.lang.InstantiationException e) {
		    				instance = NO_INSTANCE_MARKER;
						}
	    			}
	    			
	    			return instance;

				}
			});

	public static Reflections getReflections() {
		return reflections;
	}

	public static <T> T getInstance( Class<T> klass ) {
		try {
			T object = (T) instancesCache.get( klass );
			if (object instanceof String && object.equals( NO_INSTANCE_MARKER )) {
				return null;
			}
			return object;
		} catch (ExecutionException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		return null;
    }

    public static Set<?> getInstances(Class<?> interfaceToImplement) {
		return reflections.getSubTypesOf( interfaceToImplement )
				.stream()
				.filter( klass -> !Modifier.isAbstract( klass.getModifiers() ))
				.map( klass -> getInstance( klass ) )
				.collect( Collectors.toSet());
    }
    
	public static String getClassDescription( Class<?> klass ) {
		ClassDescription description = klass.getAnnotation(ClassDescription.class);
		String label = description != null ? description.label() : klass.getName();
		return label;
	}
    
	public static <T> T createInstance( Class<T> abstractClass, Object parameter ) {
		Set<Class<? extends T>> klasses = DynamoObjectFactory.getReflections().getSubTypesOf( abstractClass );
		for (Class<?> klass : klasses) {
			if (Modifier.isAbstract( klass.getModifiers() )) {
				continue;
			}
			Constructor<?>[] constructors = klass.getConstructors();
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterTypes() != null && constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].equals( parameter.getClass() )) {
					try {
						return (T) constructor.newInstance( parameter );
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			}
		}
		
		return null;
	}
  
}
