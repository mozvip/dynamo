package dynamo.core.services;

import java.util.List;

public class ConfigItem {
	
	private String key;
	private String value;
	private List<ConfigItemPath> folders;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public List<ConfigItemPath> getFolders() {
		return folders;
	}
	public void setFolders(List<ConfigItemPath> folders) {
		this.folders = folders;
	}

}
