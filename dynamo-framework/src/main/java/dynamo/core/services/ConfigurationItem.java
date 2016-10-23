package dynamo.core.services;

import java.lang.reflect.Modifier;
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
	private String ifExpression;
	private Class<?> type;
	private String value;
	private boolean list;
	private boolean set;
	private boolean folder;
	private Map<String, String> allowedValues;
	private boolean required;

	public ConfigurationItem(String key, String ifExpression, Class<?> type, String defaultStringValue, boolean required, boolean list, boolean set, boolean folder) {
		this.key = key;
		this.ifExpression = ifExpression != null && !ifExpression.equals("") ? ifExpression : null;;
		this.type = type;
		this.value = defaultStringValue != null && !defaultStringValue.equals("__NULL__") ? defaultStringValue : null;
		this.list = list;
		this.set = set;
		this.folder = folder;
		this.required = required;
		
		if (type.isEnum()) {
			allowedValues = new HashMap<>();
			
			Enum[] enumValArr = (Enum[]) type.getEnumConstants();
			for (Enum enumValue : enumValArr) {
				String label = enumValue instanceof Labelized ? ((Labelized)enumValue).getLabel() : enumValue.name();
				allowedValues.put( enumValue.name(), label );
			}
		} else if (type.getName().startsWith("dynamo.")) {
			
			Set<?> klasses = DynamoObjectFactory.getReflections().getSubTypesOf( type );
			if (klasses != null && !klasses.isEmpty()) {
				allowedValues = new HashMap<>();
				for (Class<?> klass: (Set<Class<?>>) klasses) {
					if (Modifier.isAbstract( klass.getModifiers() )) {
						continue;
					}
					String label = DynamoObjectFactory.getClassDescription(klass);
					allowedValues.put( klass.getName(), label );
				}
			}
			
		}
	}
	
	public String getKey() {
		return key;
	}

	public String getIfExpression() {
		return ifExpression;
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
	
	public boolean isRequired() {
		return required;
	}
	
	public boolean isFolder() {
		return folder;
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
