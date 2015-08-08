package hclient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.RedirectLocations;

import core.WebDocument;

public class SimpleResponse implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int code;
	private byte[] byteContents;
	private String responseCharSet = "UTF-8";
	private String contentType;
	private String fileName;
	private URI lastRedirectLocation;
	
	private URL url;

	private List<URI> redirectLocations;

	public SimpleResponse( URL url, int statusCode, byte[] byteContents, String fileName, String contentType, Charset charset) {
		this.url = url;
		this.code = statusCode;
		this.byteContents = byteContents;
		this.responseCharSet = charset != null ? charset.name() : "UTF-8";
		this.contentType = contentType;
		this.fileName = fileName;
	}

	public int getCode() {
		return code;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getByteContents() {
		return byteContents;
	}

	public void setByteContents(byte[] byteContents) {
		this.byteContents = byteContents;
	}
	
	public String getContentType() {
		return contentType;
	}

	public String getStringContents() throws UnsupportedEncodingException {
		return new String( byteContents, responseCharSet );
	}
	
	public String getStringContents( Charset charset ) throws UnsupportedEncodingException {
		return new String( byteContents, charset );
	}

	public InputStream newStream() {
		return new ByteArrayInputStream( byteContents );
	}

	public List<URI> getRedirectLocations() {
		return redirectLocations;
	}

	public void setRedirectLocations(RedirectLocations redirectLocations) {
		if ( redirectLocations != null ) {
			this.redirectLocations = new ArrayList<URI> ( redirectLocations.getAll().size() );
			this.redirectLocations.addAll( redirectLocations.getAll() );
			lastRedirectLocation = this.redirectLocations.get( this.redirectLocations.size() - 1 );
		} else {
			this.redirectLocations = null;
		}
	}
	
	public URI getLastRedirectLocation() {
		return lastRedirectLocation;
	}
	
	public String getLastRedirectLocationURL() throws MalformedURLException {
		return lastRedirectLocation.toString();
	}

	public void setLastRedirectLocation(URI lastRedirectLocation) {
		this.lastRedirectLocation = lastRedirectLocation;
	}
	
	public WebDocument getDocument() throws UnsupportedEncodingException {
		return new WebDocument( url.toString(), new String(getByteContents(), responseCharSet) );
	}

}
