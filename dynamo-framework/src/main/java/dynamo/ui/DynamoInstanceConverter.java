package dynamo.ui;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import dynamo.core.manager.DynamoObjectFactory;

@FacesConverter(value="DynamoInstanceConverter")
public class DynamoInstanceConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		try {
			return DynamoObjectFactory.getInstance( Class.forName(value));
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value instanceof String) {
			return (String) value;
		}
		return value.getClass().getName();
	}

}
