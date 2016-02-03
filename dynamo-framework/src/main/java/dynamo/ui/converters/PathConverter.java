package dynamo.ui.converters;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
		if (value == null) {
			return null;
		}
		
		try {
			URL url = new URL( value );
			return Paths.get( url.toURI() );
		} catch (MalformedURLException | URISyntaxException e) {
			return Paths.get( value );
		}
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
