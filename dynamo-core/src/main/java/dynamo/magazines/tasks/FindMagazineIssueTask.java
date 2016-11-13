package dynamo.magazines.tasks;

import dynamo.magazines.model.MagazineIssue;
import dynamo.model.backlog.core.FindDownloadableTask;

public class FindMagazineIssueTask extends FindDownloadableTask<MagazineIssue> {
	
	public FindMagazineIssueTask(MagazineIssue issue) {
		super(issue);
	}
	
	@Override
	public String toString() {
		return String.format("Searching for Magazine issue : %s", downloadable.toString());
	}

}
