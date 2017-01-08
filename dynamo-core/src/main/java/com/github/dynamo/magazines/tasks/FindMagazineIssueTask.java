package com.github.dynamo.magazines.tasks;

import com.github.dynamo.magazines.model.MagazineIssue;
import com.github.dynamo.model.backlog.core.FindDownloadableTask;

public class FindMagazineIssueTask extends FindDownloadableTask<MagazineIssue> {
	
	public FindMagazineIssueTask(MagazineIssue issue) {
		super(issue);
	}
	
	@Override
	public String toString() {
		return String.format("Searching for Magazine issue : %s", downloadable.toString());
	}

}
