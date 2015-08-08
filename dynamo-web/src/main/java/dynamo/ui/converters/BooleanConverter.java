package dynamo.ui.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value="BooleanConverter")
public class BooleanConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		return value != null ? Boolean.valueOf( value ) : null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		return value != null ? Boolean.toString( ((boolean)value) ) : null;
	}

}

