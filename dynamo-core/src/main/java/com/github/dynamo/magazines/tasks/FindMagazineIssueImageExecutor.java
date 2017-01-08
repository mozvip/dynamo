package com.github.dynamo.magazines.tasks;

import java.nio.file.Path;

import com.github.dynamo.backlog.tasks.core.FindDownloadableImageExecutor;
import com.github.dynamo.backlog.tasks.core.FindDownloadableImageTask;
import com.github.dynamo.magazines.model.MagazineIssue;

public class FindMagazineIssueImageExecutor extends FindDownloadableImageExecutor<MagazineIssue> {

	public FindMagazineIssueImageExecutor(FindDownloadableImageTask<MagazineIssue> task) {
		super(task);
	}

	@Override
	public boolean downloadImageTo(Path localImage) {
		// TODO Auto-generated method stub
		return false;
	}

}
