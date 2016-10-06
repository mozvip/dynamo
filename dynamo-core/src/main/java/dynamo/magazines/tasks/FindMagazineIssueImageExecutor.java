package dynamo.magazines.tasks;

import java.nio.file.Path;

import dynamo.backlog.tasks.core.FindDownloadableImageExecutor;
import dynamo.backlog.tasks.core.FindDownloadableImageTask;
import dynamo.magazines.model.MagazineIssue;

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
