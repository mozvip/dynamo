package dynamo.webapps.acoustid;

import java.util.List;

public class AcoustIdLookupResults {
	
	private String status;
	
	private List<AcoustIdLookupResult> results;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<AcoustIdLookupResult> getResults() {
		return results;
	}

	public void setResults(List<AcoustIdLookupResult> results) {
		this.results = results;
	}
	
	

}
