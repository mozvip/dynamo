package hclient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import core.FileNameUtils;
import core.RegExp;
import core.WebDocument;
import hclient.cache.ClientCache;
import hclient.json.SerializedCookie;

public class HTTPClient {
	
	private final static Logger logger = LoggerFactory.getLogger( HTTPClient.class );
	private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0";
	private final static int TIMEOUT = 60 * 1000;

	private ClientCache cache = ClientCache.getInstance();

	private CloseableHttpClient apacheClient;

	private CookieStore cookieStore = new BasicCookieStore();
		
	public static final long REFRESH_ONE_HOUR	= 60 * 60 * 1000;
	public static final long REFRESH_ONE_DAY	= REFRESH_ONE_HOUR * 24;
	public static final long REFRESH_ONE_WEEK	= REFRESH_ONE_DAY * 7;
	public static final long REFRESH_ONE_MONTH	= REFRESH_ONE_DAY * 30;
	
	static class SingletonHolder {
		static HTTPClient instance = new HTTPClient();
	}
	
	public static HTTPClient getInstance() {
		return SingletonHolder.instance;
	}
	
	public void addCookie( BasicClientCookie cookie ) {		
		cookieStore.addCookie( cookie );
	}
	
	public void addCookie( String domain, String name, String value ) {
		BasicClientCookie cookie = new BasicClientCookie(name, value);
		setDomain(cookie, domain);
		cookieStore.addCookie( cookie );
	}
	
	public void addCookie( String domain, String path, String name, String value, Date expiryDate ) {
		BasicClientCookie cookie = new BasicClientCookie(name, value);
		setDomain(cookie, domain);	
		cookie.setPath(path);
		cookie.setExpiryDate(expiryDate);
		cookieStore.addCookie( cookie );
	}

	private void setDomain(BasicClientCookie cookie, String domain) {
		if (domain.startsWith(".")) {
			domain = domain.substring(1);
		}
		cookie.setDomain( domain );	cookie.setAttribute(ClientCookie.DOMAIN_ATTR, domain);
	}

	public void clearCookies() {
		cookieStore.clear();
	}

	private CloseableHttpClient buildClient( HttpHost proxyHost ) {
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setUserAgent( USER_AGENT );
		
		builder.setDefaultCookieStore( cookieStore );

		builder.addInterceptorFirst(new RequestAcceptEncoding());
		builder.addInterceptorFirst(new ResponseContentEncoding());
		
		builder.setRedirectStrategy( new BugFixedRedirectStrategy() );

		Builder defaultRequestConfigBuilder = RequestConfig.custom()
			    .setSocketTimeout(TIMEOUT)
			    .setConnectTimeout(TIMEOUT)
			    .setConnectionRequestTimeout(TIMEOUT);
		
		if ( proxyHost != null ) {
			defaultRequestConfigBuilder.setProxy( proxyHost );
		}
		
		builder.setDefaultRequestConfig( defaultRequestConfigBuilder.build() );

		try {
			SSLContext context = SSLContexts.custom().loadTrustMaterial( new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(context, new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});			
			builder.setSSLSocketFactory( sslsf );
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return builder.build();
	}
	
	private HTTPClient() {
		
//		System.setProperty("java.net.useSystemProxies", "true");
//		System.setProperty("http.nonProxyHosts", "localhost|127.*|10.*|[::1]");		
//
//		List<Proxy> l = null;
//		try {
//			l = ProxySelector.getDefault().select(new URI("http://www.google.com"));
//		} catch (URISyntaxException e) {
//		}
//		if (l != null) {
//			for (Proxy proxy : l) {
//		    	InetSocketAddress addr = (InetSocketAddress) proxy.address();
//		    	if (addr != null) {
//		    		System.setProperty("http.proxyHost", addr.getHostName());
//		    		System.setProperty("http.proxyPort", Integer.toString(addr.getPort()));
//		    	}
//		    }
//		}
		
		String proxyHostName = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");

		HttpHost proxyHost = null;

		if (StringUtils.isNotBlank( proxyHostName ) && StringUtils.isNumeric( proxyPort )) {
			proxyHost = new HttpHost( proxyHostName, Integer.parseInt( proxyPort ) );
		}
		
		apacheClient = buildClient( proxyHost );
		Path cookiesFile = Paths.get("cookies.json");
		if (Files.isReadable(cookiesFile)) {
			try {
				readCookies(cookiesFile);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				try {
					Files.deleteIfExists( cookiesFile );
				} catch (IOException eDelete) {
					logger.error(e.getMessage(), eDelete);
				}
			}
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        try {
					serializeCookies();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
		    }
		});

	}
	
	public synchronized void readCookies(Path cookiesFile) throws JsonParseException, JsonMappingException, IOException  {
		
		ObjectMapper mapper = new ObjectMapper();
		JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, SerializedCookie.class);
		List<SerializedCookie> cookies = mapper.readValue( new File("cookies.json"), type);
		for (SerializedCookie cookie : cookies) {
			BasicClientCookie clientCookie = new BasicClientCookie( cookie.getName(), cookie.getValue() );
			
			if (cookie.getExpiryDate() > 0) {
				clientCookie.setExpiryDate( new Date( cookie.getExpiryDate() ));
			}
			clientCookie.setVersion( cookie.getVersion() );
			setDomain( clientCookie, cookie.getDomain() );
			clientCookie.setComment( cookie.getComment() );
			clientCookie.setPath( cookie.getPath() );
			clientCookie.setSecure( cookie.isSecure() );
			
			cookieStore.addCookie( clientCookie );
		}
	}	
	
	public synchronized void serializeCookies() throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue( new File( "cookies.json"), getCookieStore().getCookies());
	}
    
    public WebDocument getXML(String url, String referer, long cacheRefreshPeriod ) throws Exception {
    	SimpleResponse contents = get( url, referer, cacheRefreshPeriod );
    	WebDocument document = new WebDocument( url, contents.getStringContents() );
    	document.setXml( true );
    	return document;
    }
    
    public InputStream getStream( String url, String referer ) throws MalformedURLException, IOException {
    	return getStream( url, referer, 0 );
    }
     
    public InputStream getStream( String url, String referer, long cacheRefreshPeriod) throws IOException {
    	SimpleResponse response = get( url, referer, cacheRefreshPeriod );
    	return new ByteArrayInputStream( response.getByteContents() );
    }
    
    public SimpleResponse get( String url, String referer, long cacheRefreshPeriod ) throws IOException {

		String key = url;

		SimpleResponse cachedContent = null;
		if (cacheRefreshPeriod > 0) {
			cachedContent = (SimpleResponse) cache.getFromCache(key);
		}
		if (cachedContent == null) {
	        // Get the value
	
			URL uURL = new URL( url );
	    	final HttpGet httpget = RequestFactory.getGet( uURL, referer );
	    	if (uURL.getHost().contains("nzbindex.nl")) {
	    		// hack for https://issues.apache.org/jira/browse/HTTPCLIENT-1550
	    		httpget.setHeader("Accept-Encoding", "gzip");
	    	}
	    	
	    	HttpContext context = new BasicHttpContext();

	    	if (httpget == null) {
	    		logger.error(String.format("Request null for URL %s", url.toString()));
	    		return null;
	    	}

			HttpResponse response = apacheClient.execute( httpget, context );
			try {
				HttpEntity entity = response.getEntity();
	
				ContentType ct = ContentType.getOrDefault( entity );
	
				String fileName;
				Header contentDisposition = response.getLastHeader("Content-Disposition");
				if (contentDisposition != null) {
					List<String> groups = RegExpMatcher.groups( contentDisposition.getValue(), ".*filename\\*?=\"(.*)\"");
					if (groups == null) {
						groups = RegExpMatcher.groups( contentDisposition.getValue(), ".*filename\\*?=(.*)");
					}
					fileName = groups.get(0);
				} else {
					fileName = FileNameUtils.sanitizeFileName( url.substring( url.lastIndexOf('/') + 1) );
				}
						
				int statusCode = response.getStatusLine().getStatusCode();
				cachedContent = new SimpleResponse( uURL, statusCode, EntityUtils.toByteArray(entity), fileName, ct.getMimeType(), ct.getCharset() );
	
				cachedContent.setRedirectLocations( (RedirectLocations) context.getAttribute( DefaultRedirectStrategy.REDIRECT_LOCATIONS) );
				
				if ( statusCode == 200 ) {
					cache.putInCache( key, cachedContent, cacheRefreshPeriod );
				}
			} finally {
				httpget.releaseConnection();
			}
		}
		
		return cachedContent;

    }
    
    public SimpleResponse get( String url ) throws IOException, URISyntaxException {
    	return get( url, null, 0 );
    }
   
    public SimpleResponse get( String url, long cacheRefreshPeriod ) throws IOException, URISyntaxException {
    	return get( url, null, cacheRefreshPeriod );
    }
    
    public SimpleResponse get( String url, String referer ) throws IOException, URISyntaxException {
    	return get( url, referer, 0 );
    }
     
    public Path download( String url ) throws IOException, URISyntaxException {
    	return download( url, null, null );
    }
       
    public Path download( String url, String referer ) throws IOException, URISyntaxException {
    	return download(url, referer, null);
    }

    public Path download( String url, String referer, Path destinationFolder ) throws IOException, URISyntaxException {
    	return download( url, referer, destinationFolder, 0 );
    }
   
    public Path downloadToFile( String url, String referer, Path destinationFile, long cacheRefreshPeriod ) throws IOException {
		SimpleResponse response = get( url, referer, cacheRefreshPeriod );
		return createFile(response, destinationFile);
    }

	private Path createFile(SimpleResponse response, Path destinationFile) throws IOException {
		Files.createDirectories( destinationFile.getParent() );
		try (InputStream input = response.newStream()) {
			Files.copy( input, destinationFile, StandardCopyOption.REPLACE_EXISTING);
		}
        return destinationFile;
	}

    public Path download( String url, String referer, Path destinationFolder, long cacheRefreshPeriod ) throws IOException {

		String fileName = FileNameUtils.sanitizeFileName( url.substring( url.lastIndexOf('/') + 1) );

		if (destinationFolder == null) {
			destinationFolder = Files.createTempDirectory("httpclient");
		}
		
		SimpleResponse response = get( url, referer, cacheRefreshPeriod );
		
		if (response.getCode() == 404) {
			return null;
		}
		
		Path destinationFile = null;
		if (!StringUtils.isEmpty( response.getFileName() )) {
			destinationFile = destinationFolder.resolve( response.getFileName() );
		} else {
			if (StringUtils.isEmpty( fileName )) {
				destinationFile = Files.createTempFile( destinationFolder, "", "" );
			} else {
				destinationFile = destinationFolder.resolve( fileName );
			}
		}
		
		return createFile(response, destinationFile);
    }

    public boolean downloadImage( URL url, File destinationFile ) throws IOException {
    	return downloadImage(url, destinationFile, 0);
    }
    
    public boolean downloadImage( URL url, File destinationFile, int minimumSize ) throws IOException {
    	    	
		HttpGet httpget = RequestFactory.getGet( url, null );
		HttpResponse response = apacheClient.execute(httpget);
		HttpEntity entity = response.getEntity();
		
		long contentLength = entity.getContentLength();
		
		if (contentLength != -1 && contentLength < minimumSize) {
			EntityUtils.consume(entity);
			return false;
		}
		
		String contentType = entity.getContentType().getValue();
		if (contentType.startsWith("image/")) {
			
			FileOutputStream output = new FileOutputStream( destinationFile );
			byte[] bytes = EntityUtils.toByteArray( entity );
			output.write( bytes );
			output.close();
			
			if (destinationFile.length() < contentLength) {
				destinationFile.delete();
			}
			
			return true;
		} else {
			EntityUtils.consume(entity);
		}
		
		return false;
    }
    
    public SimpleResponse post( String url, String referer, Map<String, Object> params) throws IOException {
    	return post( new URL(url), referer != null ? new URL( referer ) : null, params, false );
    }

    public SimpleResponse post( String url, String referer, Map<String, Object> params, boolean ajax) throws IOException {
    	return post( new URL(url), referer != null ? new URL( referer ) : null, params, ajax );
    }

    public SimpleResponse post( URL url, URL referer, Map<String, Object> params, boolean ajax ) throws IOException {

        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
    	for (Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Object> entry = iterator.next();
			String name = entry.getKey();
			if (entry.getValue() instanceof File ) {
				// partsList.add( new FilePart(name, (File) entry.getValue()) ) ;
			} else {
		        nvps.add(new BasicNameValuePair(name, entry.getValue().toString()));
			}
		}
  
    	return post( url, referer, new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")), ajax );
    }
    
    protected SimpleResponse post( URL url, URL referer, AbstractHttpEntity postEntity, boolean ajax ) throws IOException {
    	HttpPost post = RequestFactory.getPost(url, referer);
        post.setEntity( postEntity );

        HttpContext context = new BasicHttpContext();
        
        if (ajax) {
	        post.setHeader("X-Requested-With", "XMLHttpRequest");
	        post.setHeader("Pragma", "no-cache");
	        post.setHeader("Cache-Control", "no-cache");
        }
    	
    	HttpResponse response = apacheClient.execute( post, context );
    	try {
    	
			Header contentDisposition = response.getLastHeader("Content-Disposition");
			String fileName = null;
			if (contentDisposition != null) {
				fileName = RegExpMatcher.groups( contentDisposition.getValue(), ".*filename=\"(.*)\"").get(0);
			}
	
			HttpEntity entity = response.getEntity();
			ContentType ct = ContentType.getOrDefault( entity );
	
	    	SimpleResponse sr = new SimpleResponse( url, response.getStatusLine().getStatusCode(), EntityUtils.toByteArray( entity ), fileName, ct.getMimeType(), ct.getCharset() );
	    	sr.setRedirectLocations( (RedirectLocations) context.getAttribute( DefaultRedirectStrategy.REDIRECT_LOCATIONS) );
	    	
	    	return sr;
    	} finally {
    		post.releaseConnection();
    	}
    }
    
    public String getCookie( String domain ) {
    	for (Cookie cookie : cookieStore.getCookies()) {
			if (StringUtils.equals(cookie.getDomain(), domain)) {
				return cookie.getName() + "=" + cookie.getValue();
			}
		}
    	return null;
    }
    
	public CookieStore getCookieStore() {
		return cookieStore;
	}
	
	public WebDocument getDocument( String url, long cacheRefreshPeriod ) throws IOException {
		return getDocument( url, null, cacheRefreshPeriod);
	}
	
	public WebDocument getDocument( String url ) throws IOException {
		return getDocument( url, null, 0);
	}	
	
	public WebDocument getDocument( String url, String referer, long cacheRefreshPeriod ) throws IOException {
		SimpleResponse response = get( url, referer, cacheRefreshPeriod );
		if (response == null) {
			return null;
		}
		return new WebDocument( response.getCode(), url.toString(), response.getStringContents(), response.getContentType() );
	}

	public Reader getReader(String url, String referer, long cacheRefreshPeriod ) throws IOException, URISyntaxException {
		SimpleResponse response = get( url, referer, cacheRefreshPeriod );
		return new StringReader( response.getStringContents() );
	}

	public void setProxy(HttpHost proxyHost) {
		apacheClient = buildClient(proxyHost);
	}
	
	public SimpleResponse post(String url, String referer, String... params) throws IOException {
		return post( new URL( url ), referer != null ? new URL(referer) : null, false, params );
	}

	public SimpleResponse postAjax(String url, String referer, String... params) throws IOException {
		return post( new URL( url ), referer != null ? new URL(referer) : null, true, params );
	}

	protected SimpleResponse post(URL url, URL referer, boolean ajax, String... parameters) throws IOException {
		Map<String, Object> paramsMap = getParamsMap( parameters );
		return post(url, referer, paramsMap, ajax);
	}
	
	protected Map<String, Object> getParamsMap( String... parameters ) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		if (parameters != null) {
			for (String param : parameters) {
				String[] keyVal = RegExp.parseGroups( param, "([#%\\w]+)=(.*)" );
				paramsMap.put( keyVal[0], keyVal[1] != null ? keyVal[1] : "" );
			}
		}
		return paramsMap;
	}

	public SimpleResponse submit(Element jsoupFormElement, String... parameters) throws IOException {
		
		Map<String, Object> paramsMap = getParamsMap( parameters );
				
		Elements inputElements = jsoupFormElement.select("input");
		for (Element input : inputElements) {
			String name = input.attr("name");
			if (StringUtils.isNotEmpty( name )) {
				String value = input.attr("value");
				if (!paramsMap.containsKey( name )) {
					if (StringUtils.isNotEmpty( value )) {
							paramsMap.put( name, value );
					}
				}
			}
		}

		String url = jsoupFormElement.baseUri();

		String method = jsoupFormElement.attr("method");
		SimpleResponse response = null;
		
		String action = jsoupFormElement.attr("action");
		
		if ( StringUtils.equalsIgnoreCase(method, "POST")) {
			String submitURL = url;
			if (action != null) {
				if (action.startsWith("/")) {
					submitURL = url + action;
				} else {
					submitURL = url.substring(0, url.lastIndexOf('/') + 1) + action;
				}
			}
			response = post( submitURL, url, paramsMap, false );
		} else {
			// TODO
		}
		return response;
	}
	
	public SimpleResponse postJSON(String url, String referer, String... parameters) throws ClientProtocolException, UnsupportedEncodingException, IOException {
		return postJSON(new URL(url), referer != null ? new URL(referer) : null, parameters);
	}

	public SimpleResponse postJSON(URL url, URL referer, String... parameters) throws ClientProtocolException, UnsupportedEncodingException, IOException {

		Map<String, Object> paramsMap = getParamsMap( parameters );
		ObjectMapper mapper = new ObjectMapper();
		String jsonRequest = mapper.writeValueAsString( paramsMap );
		
		return post(url, referer, new StringEntity( jsonRequest ), false );

	}

	public HttpResponse execute(HttpUriRequest request) throws IOException {
		return apacheClient.execute(request);
	}

	public void removeCache(String url) {
		cache.removeCache( url );
	}

}
