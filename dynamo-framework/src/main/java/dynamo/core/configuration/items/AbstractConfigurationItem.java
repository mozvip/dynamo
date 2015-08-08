package dynamo.core.configuration.items;

import java.lang.reflect.Field;

import javax.el.ExpressionFactory;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.configuration.Configurable;
import dynamo.core.el.DynamoELContext;
import dynamo.core.manager.ConfigurationManager;

public abstract class AbstractConfigurationItem {

	private String key;
	private String label;
	private String category;

	private String requiredExpr;
	private String disabledExpr;

	private String stringValue;
	protected Class type;
	
	private String defaultLabel = "None";

	private Class configuredClass;
	
	private boolean bold;
	
	public String getViewId() {
		return getKey().replace('.', '_');
	}

	public AbstractConfigurationItem( String key, Configurable configurable, Field field, Class configuredClass ) {

		this.key = key;

		String defaultValue = null;

		if (configurable != null) {
			this.bold = configurable.bold();
			this.label = configurable.name();
			this.category = configurable.category();
			this.requiredExpr = configurable.required();
			this.disabledExpr = configurable.disabled();
			defaultValue = configurable.defaultValue();
			defaultLabel = configurable.defaultLabel();
			if (StringUtils.equals( defaultValue, "__NULL__")) {
				defaultValue = null;
			}
		}

		this.configuredClass = configuredClass;

		if (StringUtils.isEmpty( this.label )) {
			this.label = ConfigurationManager.getInstance().getLabel( this.key );
		}
		if ( field != null ) {
			this.type = field.getType();
		}

		setStringValue( ConfigurationManager.getInstance().getConfigString(key, defaultValue) );
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Class getConfiguredClass() {
		return configuredClass;
	}

	public String getPrefix() {
		return key.substring(0, key.indexOf('.'));
	}
	
	protected Object eval( Object thisObject, String expression, Class expectedClass ) {
		DynamoELContext context = new DynamoELContext( thisObject );
		return ExpressionFactory.newInstance().createValueExpression( context, expression, expectedClass ).getValue( context );
	}	

	protected Object eval( String expression, Class expectedClass ) {
		DynamoELContext context = new DynamoELContext( configuredClass );
		return ExpressionFactory.newInstance().createValueExpression( context, expression, expectedClass ).getValue( context );
	}

	public boolean isDisabled() {
		if (disabledExpr != null) {
			return (boolean) eval(disabledExpr, boolean.class );
		}
		return false;
	}

	public boolean isRequired() {
		if ( requiredExpr != null) {
			return (boolean) eval(requiredExpr, boolean.class );
		}
		return false;
	}	

	public String getDisabledExpr() {
		return disabledExpr;
	}

	public String getRequiredExpr() {
		return requiredExpr;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Class getType() {
		return type;
	}

	public void setType(Class type) {
		this.type = type;
	}

	public void refresh() {
	}

	public abstract Object getValueFromString( String value ) throws ClassNotFoundException;
	public abstract String toStringValue( Object value );
	
	public Object getValue() throws ClassNotFoundException {
		return getValueFromString( getStringValue() );
	}

	public void setValue( Object value ) {
		setStringValue( toStringValue(value) );
	}

	public boolean isBold() {
		return bold;
	}
	
	public void setBold(boolean bold) {
		this.bold = bold;
	}

	public String getDefaultLabel() {
		return defaultLabel;
	}
	
	public boolean hasError() {
		return isRequired() && StringUtils.isBlank( stringValue );
	}

}
