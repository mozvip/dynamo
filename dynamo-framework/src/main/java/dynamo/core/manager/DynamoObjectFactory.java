package dynamo.core.manager;

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
		Set<Class<? extends T>> classes = reflections.getSubTypesOf( interfaceToImplement );
		return classes.stream().filter( klass -> !Modifier.isAbstract( klass.getModifiers() )).map( klass -> getInstance(klass)).collect( Collectors.toSet());
    }
  
}
