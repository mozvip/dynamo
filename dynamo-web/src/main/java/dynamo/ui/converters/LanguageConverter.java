package dynamo.ui.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.Language;

@FacesConverter(value="LanguageConverter")
public class LanguageConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		return StringUtils.isNotBlank(value) ? Language.valueOf( value ) : null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		if (value instanceof String) {
			return (String) value;
		}
		return value != null ? ((Language)value).name() : null;
	}

}
