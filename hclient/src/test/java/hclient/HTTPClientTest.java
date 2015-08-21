package hclient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import core.WebDocument;
import junit.framework.Assert;

public class HTTPClientTest {
	
	private HTTPClient client = HTTPClient.getInstance();
	
	@BeforeClass
	public static void init() {
	}

	@Test
	public void testGetZip() throws IOException, URISyntaxException {
		SimpleResponse response = HTTPClient.getInstance().get( "http://www.dzone.com/sites/all/files/Log4jExample4.zip" );
		Assert.assertEquals(response.getContentType(), "application/zip");
	}
	
	@Test
	public void testGetReddit() throws Exception {
		WebDocument document = HTTPClient.getInstance().getDocument( "http://www.reddit.com/r/programming/", 0 );
		List<Node> titles = document.evaluateXPath("//a[contains(@class, 'title')]/text()");
		Assert.assertTrue( titles.size() > 0 );
	}

	@Test
	public void testAmazonCookies() throws IOException, URISyntaxException {
		client.getDocument("http://www.amazon.fr", 0);
		List<Cookie> cookies = client.getCookieStore().getCookies();
		Assert.assertTrue( cookies.size() > 0 );
	}

}
