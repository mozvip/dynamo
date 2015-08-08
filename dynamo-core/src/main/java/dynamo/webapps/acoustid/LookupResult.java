package dynamo.webapps.acoustid;

import java.util.List;

public class LookupResult {

	private float score;
	private String id;
	private List<Recording> recordings;
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

}
