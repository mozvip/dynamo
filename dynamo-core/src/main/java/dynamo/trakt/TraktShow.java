package dynamo.trakt;

import java.util.List;
import java.util.Map;

public class TraktShow {
	
	private String title;
	private int year;
	private String url;
	private Map<String, String> ids;
	private Map<String, String> images;
	private List<String> genres;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Map<String, String> getIds() {
		return ids;
	}
	public void setIds(Map<String, String> ids) {
		this.ids = ids;
	}
	public Map<String, String> getImages() {
		return images;
	}
	public void setImages(Map<String, String> images) {
		this.images = images;
	}
	public List<String> getGenres() {
		return genres;
	}
	public void setGenres(List<String> genres) {
		this.genres = genres;
	}

}
