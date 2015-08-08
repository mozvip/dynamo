package dynamo.ui.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter(value="ClassConverter")
public class ClassConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		if ("".equals(value)) {
			return null;
		}
		try {
			return Class.forName( value );
		} catch (ClassNotFoundException e) {
			throw new ConverterException(e);
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		if (value instanceof String) return (String)value;
		return ((Class)value).getName();
	}

}
