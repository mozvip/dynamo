package dynamo.webapps.sabnzbd;

import java.nio.file.Path;

import dynamo.backlog.tasks.nzb.AbstractNZBDownloadExecutor;
import dynamo.backlog.tasks.nzb.DownloadNZBTask;
import dynamo.core.configuration.ClassDescription;
import dynamo.jdbi.SearchResultDAO;

@ClassDescription(label="SabNZBD+")
public class SabNzbdDownloadExecutor extends AbstractNZBDownloadExecutor {

	public SabNzbdDownloadExecutor(DownloadNZBTask task, SearchResultDAO searchResultDAO) {
		super(task, searchResultDAO);
	}

	@Override
	public String handleNZBFile(Path nzbFilePath) throws Exception {
		// FIXME
		String niceName = nzbFilePath.getFileName().toString();
		return SabNzbd.getInstance().addNZB(niceName, nzbFilePath);
	}

}
