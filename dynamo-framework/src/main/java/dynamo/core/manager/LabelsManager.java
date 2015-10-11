package dynamo.core.manager;

import java.util.ResourceBundle;

public class LabelsManager {
	
	private static ResourceBundle bundle = ResourceBundle.getBundle("labels");
	
	static class SingletonHolder {
		static LabelsManager instance = new LabelsManager();
	}

	public static LabelsManager getInstance() {
		return SingletonHolder.instance;
	}	
	
	private LabelsManager() {
	}

	public static String getLabel(String key) {
		if (bundle != null && bundle.containsKey(key)) {
			return bundle.getString(key);
		}
		return key;
	}
	
}
