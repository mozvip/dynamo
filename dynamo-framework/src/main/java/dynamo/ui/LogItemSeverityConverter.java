package dynamo.ui;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import dynamo.core.logging.LogItemSeverity;

@FacesConverter(value="LogItemSeverityConverter")
public class LogItemSeverityConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return LogItemSeverity.valueOf( value );
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return ((LogItemSeverity)value).name();
	}

}
