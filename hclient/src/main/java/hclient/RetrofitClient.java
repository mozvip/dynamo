package hclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedOutput;

public class RetrofitClient implements Client {

	@Override
	public Response execute(Request request) throws IOException {
		HttpUriRequest apacheRequest = createRequest(request);
		HttpResponse apacheResponse = HTTPClient.getInstance().execute( apacheRequest );
		return parseResponse( request.getUrl(), apacheResponse );
	}

	static HttpUriRequest createRequest(Request request) {
		return new GenericHttpRequest(request);
	}

	static Response parseResponse(String url, HttpResponse response) throws IOException {
		StatusLine statusLine = response.getStatusLine();
		int status = statusLine.getStatusCode();
		String reason = statusLine.getReasonPhrase();

		List<Header> headers = new ArrayList<Header>();
		String contentType = "application/octet-stream";
		for (org.apache.http.Header header : response.getAllHeaders()) {
			String name = header.getName();
			String value = header.getValue();
			if ("Content-Type".equalsIgnoreCase(name)) {
				contentType = value;
			}
			headers.add(new Header(name, value));
		}

		TypedByteArray body = null;
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			body = new TypedByteArray(contentType, EntityUtils.toByteArray(entity));
		}

		return new Response( url, status, reason, headers, body );
	}

	private static class GenericHttpRequest extends
			HttpEntityEnclosingRequestBase {
		private final String method;

		GenericHttpRequest(Request request) {
			super();
			method = request.getMethod();
			setURI(URI.create(request.getUrl()));

			// Add all headers.
			for (Header header : request.getHeaders()) {
				if (!header.getName().equals("Content-Length")) {
					addHeader(new BasicHeader(header.getName(), header.getValue()));
				}
			}

			// Add the content body, if any.
			TypedOutput body = request.getBody();
			if (body != null) {
				setEntity(new TypedOutputEntity(body));
			}
		}

		@Override
		public String getMethod() {
			return method;
		}
	}

	/**
	 * Container class for passing an entire {@link TypedOutput} as an
	 * {@link HttpEntity}.
	 */
	static class TypedOutputEntity extends AbstractHttpEntity {
		final TypedOutput typedOutput;

		TypedOutputEntity(TypedOutput typedOutput) {
			this.typedOutput = typedOutput;
			setContentType(typedOutput.mimeType());
		}

		@Override
		public boolean isRepeatable() {
			return true;
		}

		@Override
		public long getContentLength() {
			return typedOutput.length();
		}

		@Override
		public InputStream getContent() throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			typedOutput.writeTo(out);
			return new ByteArrayInputStream(out.toByteArray());
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			typedOutput.writeTo(out);
		}

		@Override
		public boolean isStreaming() {
			return false;
		}
	}

}
