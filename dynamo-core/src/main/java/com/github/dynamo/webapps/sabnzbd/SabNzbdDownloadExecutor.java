package com.github.dynamo.webapps.sabnzbd;

import java.nio.file.Path;

import com.github.dynamo.backlog.tasks.nzb.AbstractNZBDownloadExecutor;
import com.github.dynamo.backlog.tasks.nzb.DownloadNZBTask;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.jdbi.SearchResultDAO;

@ClassDescription(label="SabNZBD+")
public class SabNzbdDownloadExecutor extends AbstractNZBDownloadExecutor {

	public SabNzbdDownloadExecutor(DownloadNZBTask task, SearchResultDAO searchResultDAO) {
		super(task, searchResultDAO);
	}

	@Override
	public String handleNZBFile(Path nzbFilePath) throws Exception {
		return SabNzbd.getInstance().addNZB(nzbFilePath);
	}

}
