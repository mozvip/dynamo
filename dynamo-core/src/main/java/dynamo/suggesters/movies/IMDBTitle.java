package dynamo.suggesters.movies;

import core.WebResource;

public class IMDBTitle {
	
	private String id;
	private String name;
	private int year;
	private float rating;
	private boolean tvSeries;
	private WebResource image;
	private boolean released;

	public IMDBTitle(String id, String name, int year, float rating, boolean tvSeries, boolean released, WebResource image) {
		super();
		this.id = id;
		this.name = name;
		this.year = year;
		this.rating = rating; 
		this.tvSeries = tvSeries;
		this.released = released;
		this.image = image;
	}
	
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getYear() {
		return year;
	}
	public float getRating() {
		return rating;
	}
	public boolean isTvSeries() {
		return tvSeries;
	}
	public boolean isReleased() {
		return released;
	}
	public WebResource getImage() {
		return image;
	}

}
