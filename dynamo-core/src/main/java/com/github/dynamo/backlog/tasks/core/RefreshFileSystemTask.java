package com.github.dynamo.backlog.tasks.core;

import com.github.dynamo.core.model.DaemonTask;

public class RefreshFileSystemTask extends DaemonTask {

	public RefreshFileSystemTask() {
		super( 60 * 24 );
	}
	
	@Override
	public String toString() {
		return "Refresh download status";
	}

}
