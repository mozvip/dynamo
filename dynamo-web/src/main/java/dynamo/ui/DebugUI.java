package dynamo.ui;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import dynamo.manager.DownloadableManager;

@ManagedBean(name="debug")
@ApplicationScoped
public class DebugUI {
	
	public void clearBlackList() {
		DownloadableManager.getInstance().clearBlackList();
	}

}
