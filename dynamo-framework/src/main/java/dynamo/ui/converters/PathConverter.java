package dynamo.ui.converters;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value="PathConverter")
public class PathConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return value != null ? Paths.get( value ) : null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		if (value instanceof String) {
			return (String) value;
		}
		return value != null ? ((Path)value).toAbsolutePath().toString() : null;
	}

}
