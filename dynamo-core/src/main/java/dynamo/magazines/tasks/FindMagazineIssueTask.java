package dynamo.magazines.tasks;

import dynamo.manager.DownloadableManager;
import dynamo.model.backlog.core.FindDownloadableTask;
import dynamo.model.magazines.MagazineIssue;

public class FindMagazineIssueTask extends FindDownloadableTask<MagazineIssue> {
	
	public FindMagazineIssueTask(MagazineIssue issue) {
		super(issue);
	}
	
	@Override
	public String toString() {
		return String.format("Searching for Magazine issue : %s", downloadable.toString());
	}
	
	@Override
	public void cancel() {
		DownloadableManager.getInstance().suggest( downloadable );
	}	

}
