package dynamo.backlog.tasks.nzb;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import com.github.mozvip.hclient.HTTPClient;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.DownloadFinder;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.result.NZB;
import dynamo.model.result.NZBFile;

public abstract class AbstractNZBDownloadExecutor extends TaskExecutor<DownloadNZBTask> {
	private SearchResultDAO searchResultDAO;

	public AbstractNZBDownloadExecutor(DownloadNZBTask task, SearchResultDAO searchResultDAO) {
		super(task);
		this.searchResultDAO = searchResultDAO;
	}

	public abstract String handleNZBFile(Path nzbFilePath) throws Exception;

	@Override
	public void execute() throws Exception {

		Path nzbFilePath = null;

		if (task.getSearchResult() != null) {
			DownloadFinder finder = DynamoObjectFactory.getInstance(task.getSearchResult().getProviderClass());
			nzbFilePath = finder.download(task.getNzbURL(), task.getSearchResult().getReferer());
		} else {
			nzbFilePath = HTTPClient.getInstance().download(task.getNzbURL(), null);
		}

		NZB nzb = new NZB(nzbFilePath);
		if (nzb.getFiles() == null) {
			throw new Exception("Downloaded file is not a NZB, will retry downloading it later");
		} else {
			
			if (task.getDownloadable() != null) {
				for (NZBFile file: nzb.getFiles()) {
					searchResultDAO.createFile(file.getName(), file.getSize(), task.getSearchResult().getUrl());
				}
				DownloadableManager.getInstance().snatched( task.getDownloadable(), task.getSearchResult() );
				String clientId = handleNZBFile(nzbFilePath);
				if (StringUtils.isNotBlank( clientId )) {
					searchResultDAO.updateClientId(task.getSearchResult().getUrl(), clientId);
				}
			}
		}
	}

	@Override
	public void rescheduleTask(DownloadNZBTask item) {
		if (isFailed()) {
			BackLogProcessor.getInstance().schedule(item, getNextDate(60), true);
		}
	}
}
