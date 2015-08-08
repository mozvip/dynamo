package core;

import java.net.URL;


public class WebResource {
	
	private String url;
	private String referer;

	public WebResource(String url, String referer) {
		super();
		this.url = url;
		this.referer = referer;
	}
	
	public WebResource(String url) {
		super();
		this.url = url;
	}	
	
	public WebResource(String url, URL referer) {
		this(url, referer.toString());
	}

	public String getUrl() {
		return url;
	}
	public String getReferer() {
		return referer;
	}
	
	@Override
	public boolean equals(Object obj) {
		WebResource otherLink = (WebResource) obj;
		return otherLink.getUrl().equals( url );	// referer does not matter
	}
	
	@Override
	public int hashCode() {
		return (url).hashCode();	// referer does not matter
	}	
	
	

}
