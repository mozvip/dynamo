package hclient;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultRedirectStrategy;

public class BugFixedRedirectStrategy extends DefaultRedirectStrategy {
	
    /**
     * Redirectable methods.
     */
    private static final String[] REDIRECT_METHODS = new String[] {
        HttpGet.METHOD_NAME,
        HttpHead.METHOD_NAME,
        HttpPost.METHOD_NAME
    };	
	
    /**
     * @since 4.2
     */
    protected boolean isRedirectable(final String method) {
        for (final String m: REDIRECT_METHODS) {
            if (m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }
	
	
	@Override
    protected URI createLocationURI(final String location) throws ProtocolException {
    	URI uri = null;
        try {
        	uri = new URI( location );
        } catch (URISyntaxException ex) {
    		try {
    			
    			String prefix = location.substring(0, location.indexOf('/', 7) + 1);
    			String suffix = URLEncoder.encode( location.substring( location.indexOf('/', 7) + 1), "UTF-8" ) ;
    			// suffix = suffix.replace("%2F", "/");
    			//String suffix = location.substring( location.indexOf('/', 7) + 1) ;
    			//suffix = suffix.replace(" ", "%20");
				uri = new URI( prefix + suffix );
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				throw new ProtocolException(e.getMessage(), e);
			}
        }
    	return uri;
    }

}
