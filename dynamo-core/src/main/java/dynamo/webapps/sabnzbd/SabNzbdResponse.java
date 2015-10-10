package dynamo.webapps.sabnzbd;

import java.util.List;

public class SabNzbdResponse {
	
	private SABHistoryResponse history;
	private boolean status;
	private List<String> nzo_ids;
	private List<Job> jobs;
	private Queue queue;

	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public List<String> getNzo_ids() {
		return nzo_ids;
	}
	public void setNzo_ids(List<String> nzo_ids) {
		this.nzo_ids = nzo_ids;
	}

	public SABHistoryResponse getHistory() {
		return history;
	}
	public void setHistory(SABHistoryResponse history) {
		this.history = history;
	}
	
	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
	
	public Queue getQueue() {
		return queue;
	}
	
	public void setQueue(Queue queue) {
		this.queue = queue;
	}
}
