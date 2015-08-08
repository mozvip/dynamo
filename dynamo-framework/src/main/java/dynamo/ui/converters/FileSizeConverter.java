package dynamo.ui.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value="FileSizeConverter")
public class FileSizeConverter implements Converter {
	
	private final long TB = 1024l*1024l*1024l*1024l;
	private final long GB = 1024l*1024l*1024l;
	private final long MB = 1024l*1024l;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return value;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value instanceof String) {
			return (String)value;
		}
		Long v = (Long) value;
		
		String suffix = "KB";
		float floatValue;
		
		if (v > TB) {
			suffix = "TB";
			floatValue = (float) v / TB;
		} else if ( v > GB ) {
			suffix = "GB";
			floatValue = (float) v / GB;
		} else if ( v > MB ) {			
			suffix = "MB";
			floatValue = (float) v / MB;			
		} else {
			floatValue = (float) v / 1024;
		}
		
		return String.format("%8.1f %s", floatValue, suffix);
	}

}
