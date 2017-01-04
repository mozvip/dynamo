package dynamo.httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.yamj.api.common.http.CommonHttpClient;
import org.yamj.api.common.http.DigestedResponse;
import org.yamj.api.common.http.IUserAgentSelector;

import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.SimpleResponse;

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
		return client.execute(request);
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

	@Override
	public void setUserAgentSelector(IUserAgentSelector userAgentSelector) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DigestedResponse postContent(URL url, HttpEntity entity) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse postContent(URL url, HttpEntity entity, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse postContent(String uri, HttpEntity entity) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse postContent(String uri, HttpEntity entity, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse postContent(URI uri, HttpEntity entity) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse postContent(URI uri, HttpEntity entity, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse postContent(HttpPost httpPost) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse postContent(HttpPost httpPost, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse deleteContent(URL url) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse deleteContent(URL url, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse deleteContent(String uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse deleteContent(String uri, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse deleteContent(URI uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse deleteContent(URI uri, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse deleteContent(HttpDelete httpDelete) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse deleteContent(HttpDelete httpDelete, Charset charset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity postResource(URL url, HttpEntity entity) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity postResource(String uri, HttpEntity entity) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity postResource(URI uri, HttpEntity entity) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity postResource(HttpPost httpPost) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity deleteResource(URL url) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity deleteResource(String uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity deleteResource(URI uri) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpEntity deleteResource(HttpDelete httpDelete) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DigestedResponse requestContent(URL url) throws IOException {
		SimpleResponse response = client.get( url.toString(), HTTPClient.REFRESH_ONE_DAY );
		return new DigestedResponse(response.getCode(), response.getStringContents());
	}

	@Override
	public DigestedResponse requestContent(URL url, Charset charset) throws IOException {
		SimpleResponse response = client.get( url.toString(), HTTPClient.REFRESH_ONE_DAY );
		return new DigestedResponse(response.getCode(), response.getStringContents(charset));
	}

	@Override
	public DigestedResponse requestContent(String uri) throws IOException {
		SimpleResponse response = client.get( uri, HTTPClient.REFRESH_ONE_DAY );
		return new DigestedResponse(response.getCode(), response.getStringContents());
	}

	@Override
	public DigestedResponse requestContent(String uri, Charset charset) throws IOException {
		SimpleResponse response = client.get( uri );
		return new DigestedResponse(response.getCode(), response.getStringContents(charset));
	}

	@Override
	public DigestedResponse requestContent(URI uri) throws IOException {
		SimpleResponse response = client.get( uri.toString() );
		return new DigestedResponse(response.getCode(), response.getStringContents());
	}

	@Override
	public DigestedResponse requestContent(URI uri, Charset charset) throws IOException {
		SimpleResponse response = client.get( uri.toString() );
		return new DigestedResponse(response.getCode(), response.getStringContents(charset));
	}

	@Override
	public DigestedResponse requestContent(HttpGet httpGet) throws IOException {
		HttpResponse response = client.execute( httpGet );
		HttpEntity entity = response.getEntity();
		return new DigestedResponse(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity));
	}

	@Override
	public DigestedResponse requestContent(HttpGet httpGet, Charset charset) throws IOException {
		HttpResponse response = client.execute( httpGet );
		HttpEntity entity = response.getEntity();
		return new DigestedResponse(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity, charset));
	}

}
