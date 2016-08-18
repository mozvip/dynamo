package dynamo.core.services;

public class ConfigurationItem {

	private String category;
	private String name;
	private String key;
	private Class<?> type;
	private String value;
	private boolean list;
	private boolean set;

	public ConfigurationItem(String category, String name, String key, Class<?> type, boolean list, boolean set) {
		super();
		this.category = category;
		this.name = name;
		this.key = key;
		this.type = type;
		this.list = list;
		this.set = set;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isList() {
		return list;
	}

	public void setList(boolean list) {
		this.list = list;
	}

	public boolean isSet() {
		return set;
	}

	public void setSet(boolean set) {
		this.set = set;
	}
	
	

}
