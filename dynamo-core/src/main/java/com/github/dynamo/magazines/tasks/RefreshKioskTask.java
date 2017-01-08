package com.github.dynamo.magazines.tasks;

import com.github.dynamo.core.model.DaemonTask;
import com.github.dynamo.magazines.MagazineManager;

public class RefreshKioskTask extends DaemonTask {

	public RefreshKioskTask() {
		super(60 * 24); // once per day
	}

	@Override
	public boolean isEnabled() {
		return MagazineManager.getInstance().isEnabled();
	}

	@Override
	public String toString() {
		return "Refresh Magazine Kiosk";
	}

}
