package dynamo.magazines.tasks;

import dynamo.core.model.DaemonTask;
import dynamo.magazines.MagazineManager;

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
