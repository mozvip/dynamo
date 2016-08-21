package dynamo.core.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import dynamo.core.Labelized;
import dynamo.core.manager.DynamoObjectFactory;

@JsonInclude(Include.NON_NULL)
public class ConfigurationItem {
	
	private String key;
	private String category;
	private String name;
	private Class<?> type;
	private String value;
	private boolean list;
	private boolean set;
	private Map<String, String> allowedValues;

	public ConfigurationItem(String key, String category, String name, Class<?> type, boolean list, boolean set) {
		this.key = key;
		this.category = category;
		this.name = name;
		this.type = type;
		this.list = list;
		this.set = set;
		
		if (type.isEnum()) {
			allowedValues = new HashMap<>();
			
			Enum[] enumValArr = (Enum[]) type.getEnumConstants();
			for (Enum enumValue : enumValArr) {
				String label = enumValue instanceof Labelized ? ((Labelized)enumValue).getLabel() : enumValue.name();
				allowedValues.put( enumValue.name(), label );
			}
		} else if (type.getName().startsWith("dynamo.")) {
			
			allowedValues = new HashMap<>();
			Set<?> instances = new DynamoObjectFactory<>( type ).getInstances();
			for (Object instance: instances) {
				String label = instance instanceof Labelized ? ((Labelized)instance).getLabel() : instance.getClass().getName();
				allowedValues.put( instance.getClass().getName(), label );
			}
			
		}
	}
	
	public String getKey() {
		return key;
	}

	public String getCategory() {
		return category;
	}

	public String getName() {
		return name;
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

	public Map<String, String> getAllowedValues() {
		return allowedValues;
	}	

}
