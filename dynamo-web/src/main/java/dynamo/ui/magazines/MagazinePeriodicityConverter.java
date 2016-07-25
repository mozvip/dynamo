package dynamo.ui.magazines;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;

import dynamo.magazines.model.MagazinePeriodicity;

@FacesConverter(value="MagazinePeriodicityConverter")
public class MagazinePeriodicityConverter implements Converter {
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		return StringUtils.isNotBlank(value) ? MagazinePeriodicity.valueOf( value ) : null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		if (value instanceof String) {
			return (String) value;
		}
		return value != null ? ((MagazinePeriodicity)value).name() : null;
	}

}
