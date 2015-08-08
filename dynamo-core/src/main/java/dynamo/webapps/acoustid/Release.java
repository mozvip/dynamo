package dynamo.webapps.acoustid;

import java.util.List;

public class Release {
	
	private String id;
	private int track_count;
	private String country;
	private int medium_count;
	private AcoustIDDate date;
	
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

}
