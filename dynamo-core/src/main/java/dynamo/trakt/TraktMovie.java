package dynamo.trakt;

import java.util.Map;
import java.util.Set;

public class TraktMovie {
	
	private String title;
	private int year;
	private int released;
	private String url;
	private String trailer;
	private int runtime;
	private String tagline;
	private String overview;
	private String certification;
	private Map<String, String> ids;
	private Map<String, String> images;
	private Map<String, Integer> ratings;
	private Set<String> genres;
	
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
	public int getReleased() {
		return released;
	}
	public void setReleased(int released) {
		this.released = released;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTrailer() {
		return trailer;
	}
	public void setTrailer(String trailer) {
		this.trailer = trailer;
	}
	public int getRuntime() {
		return runtime;
	}
	public void setRuntime(int runtime) {
		this.runtime = runtime;
	}
	public String getTagline() {
		return tagline;
	}
	public void setTagline(String tagline) {
		this.tagline = tagline;
	}
	public String getOverview() {
		return overview;
	}
	public void setOverview(String overview) {
		this.overview = overview;
	}
	public String getCertification() {
		return certification;
	}
	public void setCertification(String certification) {
		this.certification = certification;
	}
	public Map<String, String> getImages() {
		return images;
	}
	public void setImages(Map<String, String> images) {
		this.images = images;
	}
	public Map<String, Integer> getRatings() {
		return ratings;
	}
	public void setRatings(Map<String, Integer> ratings) {
		this.ratings = ratings;
	}
	public Set<String> getGenres() {
		return genres;
	}
	public void setGenres(Set<String> genres) {
		this.genres = genres;
	}
	public Map<String, String> getIds() {
		return ids;
	}
	public void setIds(Map<String, String> ids) {
		this.ids = ids;
	}

}
