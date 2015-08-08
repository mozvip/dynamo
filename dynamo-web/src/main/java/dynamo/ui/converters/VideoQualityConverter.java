package dynamo.ui.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.VideoQuality;

@FacesConverter(value="VideoQualityConverter")
public class VideoQualityConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return StringUtils.isNotBlank(value) ? VideoQuality.valueOf( value ) : null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value instanceof String) {
			return (String) value;
		}
		return value != null ? ((VideoQuality)value).name() : null;
	}

}
