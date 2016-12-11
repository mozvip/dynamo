package dynamo.webapps.acoustid;

import java.util.List;

public class AcoustIdLookupResult {

	private float score;
	private String id;
	private List<Recording> recordings;
	private List<ReleaseGroup> releasegroups;
	private List<Release> releases;

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Recording> getRecordings() {
		return recordings;
	}

	public void setRecordings(List<Recording> recordings) {
		this.recordings = recordings;
	}

	public List<ReleaseGroup> getReleasegroups() {
		return releasegroups;
	}

	public void setReleasegroups(List<ReleaseGroup> releasegroups) {
		this.releasegroups = releasegroups;
	}

	public List<Release> getReleases() {
		return releases;
	}

	public void setReleases(List<Release> releases) {
		this.releases = releases;
	}

}
