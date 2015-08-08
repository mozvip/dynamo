package dynamo.core.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import core.RegExp;

public class DynamoObjectFactory<T> {
	
	private static Map<String, Set<String>> classNamesForPackage = new HashMap<>();
    private static Map<Class<?>, Object> instancesMap = new HashMap<>();

    private Class<T> interfaceToImplement = null;
	private String packageName;
	private String nameRegExp = null;
	
	public DynamoObjectFactory( String packageName, String nameRegExp ) {
		this.interfaceToImplement = null;
		this.packageName = packageName;
		this.nameRegExp = nameRegExp;
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

    /**
     * Scans all classes accessible from the class loader which belong to the given package and implements the given Interface.
     *
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Set<Class<? extends T>> getMatchingClasses( boolean includeAbstracts, boolean includeInterfaces ) {
    	
    	Set<Class<? extends T>> classes = new HashSet<>();
    	
    	if ( packageName != null ) {
    		
    		String currentPackagePrefix = null;
    		for (String packagePrefix : classNamesForPackage.keySet()) {
				if (packageName.startsWith( packagePrefix )) {
					if (currentPackagePrefix == null || currentPackagePrefix.length() < packagePrefix.length() ) {
						currentPackagePrefix = packagePrefix;
					}
				}
			}
    		
    		if (currentPackagePrefix != null) {
	    		Set<String> classesToTest = classNamesForPackage.get( currentPackagePrefix );
	    		for (String className : classesToTest) {
	    			testClass(classes, className, includeAbstracts, includeInterfaces, null );
				}
	    		return classes;
    		}
    	}
    	
    	Set<String> testedClassNames = new HashSet<>();
    	
    	synchronized ( this ) {
        	if (classLoaderURLs == null) {
        		initClassLoaderURLs();
        	}
		}

		for (URL url : classLoaderURLs) {

			String file = url.getFile();

			if (File.separator.equals("\\") && file.startsWith("/")) {
				file = file.substring(1);
			}
			
			boolean mustDeleteFile = false;
			if (file.endsWith(".jar") || file.endsWith(".jar!/")) {
				
				Path pathToFile = Paths.get( file );
				if (!Files.isRegularFile( pathToFile )) {
					try {
						pathToFile = Files.createTempFile("dynamo", ".jar");
					} catch (IOException e) {
						ErrorManager.getInstance().reportThrowable(e);
						continue;
					}
	
					try (OutputStream output = Files.newOutputStream( pathToFile )) {
						IOUtils.copy(url.openStream(), output);
					} catch (IOException e) {
						ErrorManager.getInstance().reportThrowable(e);
						continue;
					}
					
					mustDeleteFile = true;
				}
				
				try (JarFile jarFile = new JarFile(pathToFile.toFile())) { // FIXME : issue with spaces in file paths ?
					Enumeration<JarEntry> enumJar = jarFile.entries();
					while (enumJar.hasMoreElements()) {
						JarEntry entry = enumJar.nextElement();
						if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
							String className = entry.getName().substring(0, entry.getName().length() - 6);
							className = className.replace('/', '.');

							testClass( classes, className, includeAbstracts, includeInterfaces, testedClassNames );
						}
					}
				} catch (FileNotFoundException e) {
					// ignored silently
				} catch (IOException e) {
					ErrorManager.getInstance().reportThrowable(e);
				} finally {
					if (mustDeleteFile) {
						try {
							Files.delete( pathToFile );
						} catch (IOException e) {
						}
					}
				}

			} else {

				Path path = Paths.get(file);
				if (Files.isDirectory(path)) {
	
					String currentPackageName = "";
					parseFolder( classes, currentPackageName, path, includeAbstracts, includeInterfaces, testedClassNames);
	
				}

			}
		}
		
		classNamesForPackage.put( packageName, testedClassNames );

        return classes;
    }

    public static <T> T getInstance( Class<T> klass ) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
    	
		T instance = null;
		synchronized (klass) {
			if (instancesMap.containsKey( klass )) {
				instance = (T) instancesMap.get( klass );
			} else {
    			try {
    				Method singletonGetInstance = klass.getMethod("getInstance");
    				if (Modifier.isStatic(singletonGetInstance.getModifiers())) {
    					instance = (T) singletonGetInstance.invoke( null );
    				}
    			} catch (NoSuchMethodException | SecurityException e) {
    			}
    			
    			if (instance == null) {
    				try {
						instance = klass.newInstance();
					} catch (java.lang.InstantiationException e) {
					}
    			}
    			if (instance != null) {
					ConfigurationManager.getInstance().configureInstance( instance );
					instancesMap.put( klass, instance );
    			}
			}
		}

		return instance;
    }
   
    public Set<T> getInstances() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Set<Class<? extends T>> classes = getMatchingClasses( false, false );
		Set<T> instances = new HashSet<T>( classes.size() ); 
		for (Class<? extends T> klass : classes) {
			instances.add( (T) getInstance( klass ) );
		}
		return instances;
    }

	public T newInstance( Object... parameters ) {
		Set<Class<? extends T>> classes = getMatchingClasses( false, false );
		
		Class<?>[] parameterTypes = new Class[ parameters.length];
		for (int i=0; i<parameterTypes.length; i++) {
			parameterTypes[i] = parameters[i] != null ? parameters[i].getClass() : null;
		}
		for (Class<? extends T> klass : classes) {
			try {
				return klass.getConstructor(parameterTypes).newInstance( parameters );
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| SecurityException e) {
				ErrorManager.getInstance().reportThrowable( e );
			} catch (NoSuchMethodException e) {
				// ignored silently
			}
		}
		return null;
	}
  
}
