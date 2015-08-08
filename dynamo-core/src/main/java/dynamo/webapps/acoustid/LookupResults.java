package dynamo.webapps.acoustid;

import java.util.List;

public class LookupResults {
	
	private String status;
	
	private List<LookupResult> results;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<LookupResult> getResults() {
		return results;
	}

	public void setResults(List<LookupResult> results) {
		this.results = results;
	}
	
	

}
