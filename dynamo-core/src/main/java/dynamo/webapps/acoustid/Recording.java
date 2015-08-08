package dynamo.webapps.acoustid;

import java.util.List;

public class Recording {
	
	private List<Artist> artists;
	private int duration;
	private String id;
	private List<ReleaseGroup> releasegroups;
	private int sources;
	private String title;
	
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Artist> getArtists() {
		return artists;
	}
	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}
	public List<ReleaseGroup> getReleasegroups() {
		return releasegroups;
	}
	public void setReleasegroups(List<ReleaseGroup> releasegroups) {
		this.releasegroups = releasegroups;
	}
	public int getSources() {
		return sources;
	}
	public void setSources(int sources) {
		this.sources = sources;
	}
	
}
