package dynamo.core;

import java.net.MalformedURLException;
import java.net.URL;

public class RemoteSubTitles {
	
	private int id;

	byte[] data;
	private String url;
	private int score;
	
	public RemoteSubTitles() {
	}
	
	public RemoteSubTitles( byte[] data, URL url, int score ) {
		super();
		this.data = data;
		this.url = url.toExternalForm().toString();
		this.score = score;
	}
	
	public RemoteSubTitles( byte[] data, String url, int score ) throws MalformedURLException {
		this( data, new URL(url), score );
	}

	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}

	public String getUrl() {
		return url;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
