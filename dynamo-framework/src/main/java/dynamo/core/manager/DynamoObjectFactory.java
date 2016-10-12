package dynamo.core.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import dynamo.core.configuration.ClassDescription;

public class DynamoObjectFactory<T> {
	
	private static Reflections reflections = new Reflections("dynamo", new FieldAnnotationsScanner(), new SubTypesScanner( false ), new TypeAnnotationsScanner());

    private Class<T> interfaceToImplement = null;
	
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
	
	public DynamoObjectFactory( Class<T> interfaceToImplement ) {
		this.interfaceToImplement = interfaceToImplement;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstance( Class<T> klass ) {
		try {
			Object object = instancesCache.get( klass );
			if (object instanceof String && object.equals( NO_INSTANCE_MARKER )) {
				return null;
			}
			return (T) object;
		} catch (ExecutionException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		return null;
    }

    public Set<T> getInstances() {
    	Set<T> instances = new HashSet<>();
		Set<Class<? extends T>> classes = reflections.getSubTypesOf( interfaceToImplement );
		for (Class<? extends T> klass : classes) {
			if ( !Modifier.isAbstract( klass.getModifiers() ) ) {
				instances.add( getInstance(klass) );
			}
		}
		return instances;
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
