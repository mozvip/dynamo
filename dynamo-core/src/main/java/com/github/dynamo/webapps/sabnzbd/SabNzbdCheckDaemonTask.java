package com.github.dynamo.webapps.sabnzbd;

import com.github.dynamo.core.model.DaemonTask;

public class SabNzbdCheckDaemonTask extends DaemonTask {
	
	@Override
	public boolean isEnabled() {
		return SabNzbd.getInstance().isEnabled();
	}

	public SabNzbdCheckDaemonTask() {
		super(5);
	}
	
	@Override
	public String toString() {
		return "Update SABnzbd status";
	}

}
