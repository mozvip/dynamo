package dynamo.webapps.acoustid;

import java.util.List;

public class Release {

	private String id;
	private int track_count;
	private String country;
	private int medium_count;
	private AcoustIDDate date;
	private String title;

	private List<Artist> artists;
	public List<Artist> getArtists() {
		return artists;
	}

	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}

	private List<ReleaseMedium> mediums;
	private List<ReleaseEvent> releaseevents;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ReleaseMedium> getMediums() {
		return mediums;
	}

	public void setMediums(List<ReleaseMedium> mediums) {
		this.mediums = mediums;
	}

	public int getTrack_count() {
		return track_count;
	}

	public void setTrack_count(int track_count) {
		this.track_count = track_count;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getMedium_count() {
		return medium_count;
	}

	public void setMedium_count(int medium_count) {
		this.medium_count = medium_count;
	}

	public AcoustIDDate getDate() {
		return date;
	}

	public void setDate(AcoustIDDate date) {
		this.date = date;
	}

	public List<ReleaseEvent> getReleaseevents() {
		return releaseevents;
	}

	public void setReleaseevents(List<ReleaseEvent> releaseevents) {
		this.releaseevents = releaseevents;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	

}
