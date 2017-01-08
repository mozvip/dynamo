package com.github.dynamo.backlog.tasks.torrent;

import com.github.dynamo.core.manager.ConfigurationManager;
import com.github.dynamo.core.model.DaemonTask;

public class TransmissionCheckDaemonTask extends DaemonTask {
	
	@Override
	public boolean isEnabled() {
		if (Transmission.getInstance().isEnabled()) {
			return ConfigurationManager.getInstance().isActive(DownloadTorrentTransmissionExecutor.class);
		}
		return false;
	}

	public TransmissionCheckDaemonTask() {
		super(2);
	}
	
	@Override
	public String toString() {
		return "Update Transmission status";
	}

}
