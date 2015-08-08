package dynamo.core.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.undertow.UndertowMessages;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;

public class DynamoResourceManager implements ResourceManager {

    /**
     * The class loader that is used to load resources
     */
    private final ClassLoader classLoader;
    /**
     * The prefix that is appended to resources that are to be loaded.
     */
    private final String prefix;
    
    LoadingCache<String, URLResource> resources = CacheBuilder.newBuilder()
    	       .maximumSize(10000)
    	       .build(
    	           new CacheLoader<String, URLResource>() {
    	             public URLResource load(String key) throws IOException {
    	            	URL url = classLoader.getResource(key);
    	            	if (url == null) {
    	            		throw new FileNotFoundException( key );
    	            	}
    	            	return new URLResource(url, url.openConnection(), key);
    	             }
    	           });

    public DynamoResourceManager(final ClassLoader loader, final Package p) {
        this(loader, p.getName().replace(".", "/"));
    }

    public DynamoResourceManager(final ClassLoader classLoader, final String prefix) {
        this.classLoader = classLoader;
        if (prefix.equals("")) {
            this.prefix = "";
        } else if (prefix.endsWith("/")) {
            this.prefix = prefix;
        } else {
            this.prefix = prefix + "/";
        }
    }

    public DynamoResourceManager(final ClassLoader classLoader) {
        this(classLoader, "");
    }

    @Override
    public Resource getResource(final String path) throws IOException {
    	
    	if (path.startsWith("/websocket/")) {
    		return null;
    	}
    	
        String modPath = path;
        if(modPath.startsWith("/")) {
            modPath = path.substring(1);
        }
        final String realPath = prefix + modPath;
        URLResource resource;
		try {
			resource = resources.get( realPath );
		} catch (ExecutionException e) {
			throw new IOException( e );
		}
        if(resource == null) {
            return null;
        } else {
            return resource;
        }

    }

    @Override
    public boolean isResourceChangeListenerSupported() {
        return false;
    }

    @Override
    public void registerResourceChangeListener(ResourceChangeListener listener) {
        throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();
    }

    @Override
    public void removeResourceChangeListener(ResourceChangeListener listener) {
        throw UndertowMessages.MESSAGES.resourceChangeListenerNotSupported();
    }


    @Override
    public void close() throws IOException {
    }

} 
