package dynamo.httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.yamj.api.common.http.CommonHttpClient;

import hclient.HTTPClient;
import hclient.SimpleResponse;

public class YAMJHttpClient implements CommonHttpClient {
	
	private HTTPClient client;
	
	public YAMJHttpClient( HTTPClient client ) {
		this.client = client;
	}

	@Override
	public HttpParams getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientConnectionManager getConnectionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request, HttpContext context)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request,
			HttpContext context) throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpUriRequest request,
			ResponseHandler<? extends T> responseHandler) throws IOException,
			ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpUriRequest request,
			ResponseHandler<? extends T> responseHandler, HttpContext context)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpHost target, HttpRequest request,
			ResponseHandler<? extends T> responseHandler) throws IOException,
			ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpHost target, HttpRequest request,
			ResponseHandler<? extends T> responseHandler, HttpContext context)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProxy(String host, int port, String username, String password) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeouts(int connectionTimeout, int socketTimeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public String requestContent(URL url) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String requestContent(URL url, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String requestContent(String uri) throws IOException {
		return requestContent(uri, Charset.defaultCharset());
	}

	@Override
	public String requestContent(String uri, Charset charset)
			throws IOException {
		try {
			return client.get(uri, HTTPClient.REFRESH_ONE_DAY).getStringContents( charset );
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public String requestContent(URI uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String requestContent(URI uri, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String requestContent(HttpGet httpGet) throws IOException {
		SimpleResponse response = client.get( httpGet.getURI().toString(), null, HTTPClient.REFRESH_ONE_DAY );
		return response.getStringContents();
	}

	@Override
	public String requestContent(HttpGet httpGet, Charset charset)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity requestResource(URL url) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity requestResource(String uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity requestResource(URI uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity requestResource(HttpGet httpGet) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
