package dynamo.core.manager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import core.RegExp;

public class DynamoObjectFactory<T> {
	
	private static Reflections reflections = new Reflections("dynamo", new FieldAnnotationsScanner(), new SubTypesScanner( false ), new TypeAnnotationsScanner());

    private Class<T> interfaceToImplement = null;
	private String packageName;
	private String nameRegExp = null;
	
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

	public DynamoObjectFactory( String packageName, String nameRegExp ) {
		this.interfaceToImplement = null;
		this.packageName = packageName;
		this.nameRegExp = nameRegExp;
	}
	
	public static Reflections getReflections() {
		return reflections;
	}
	
	public DynamoObjectFactory( String packageName, Class<T> interfaceToImplement ) {
		this.interfaceToImplement = interfaceToImplement;
		this.packageName = packageName;
	}
	
	public DynamoObjectFactory( String packageName ) {
		this.packageName = packageName;
	}

	protected void parseFolder( Collection<Class<? extends T>> classes, String startingPackageName, Path folder, boolean includeAbstracts, boolean includeInterfaces, Set<String> testedClassNames ) {
		try (DirectoryStream<Path> ds =  Files.newDirectoryStream( folder )) {
			for (Path p : ds) {

				String fileName = p.getFileName().toString();

				if (Files.isDirectory( p )) {
					String currentPackageName;
					if (StringUtils.isEmpty( startingPackageName )) {
						currentPackageName = fileName;
					} else {
						currentPackageName = startingPackageName + "." + fileName;
					}
					if (currentPackageName.startsWith(packageName) || packageName.startsWith( currentPackageName )) {
						parseFolder( classes, currentPackageName, p, includeAbstracts, includeInterfaces, testedClassNames );
					}
				} else if ( fileName.endsWith(".class")) {
					
					String className = startingPackageName + "." + fileName.substring(0, fileName.lastIndexOf("."));
					testClass( classes, className, includeAbstracts, includeInterfaces, testedClassNames );
				}
			}
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}

	protected void testClass( Collection<Class<? extends T>> classes, String className, boolean includeAbstracts, boolean includeInterfaces, Collection<String> testedClassNames ) {
		if (testedClassNames != null) {
			testedClassNames.add( className );
		}
		if (packageName == null || className.startsWith( packageName )) {
			if (nameRegExp == null || RegExp.matches(className, nameRegExp)) {
				try {
					Class<? extends T> klass = (Class<? extends T>) Class.forName( className );
					if (klass.isAnnotation()) {
						// do not include annotations
						return;
					}
					if (includeInterfaces || (!klass.isInterface())) {
						if (includeInterfaces || ( includeAbstracts || (!Modifier.isAbstract( klass.getModifiers()) ) ) ) {
				        	if ( interfaceToImplement == null || interfaceToImplement.isAssignableFrom( klass ) ) { 
				        		classes.add( klass );
				        	}	            		
						}
					}
				} catch (java.lang.NoClassDefFoundError e ) {
				} catch (ClassNotFoundException e) {
				}
			}
		}
	}
	
	private static Set<URL> classLoaderURLs;
	
	public static synchronized void initClassLoaderURLs() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		classLoaderURLs = new HashSet<URL>();
		while (classLoader != null) {
			if (classLoader instanceof java.net.URLClassLoader) {
				@SuppressWarnings("resource")
				java.net.URLClassLoader cl = (java.net.URLClassLoader) classLoader;
				for (URL url : cl.getURLs()) {
					classLoaderURLs.add(url);
				}
			}
        	classLoader = classLoader.getParent();
		}
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
