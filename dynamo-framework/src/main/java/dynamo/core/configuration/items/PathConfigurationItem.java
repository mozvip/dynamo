package dynamo.core.configuration.items;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

import dynamo.core.configuration.Configurable;

public class PathConfigurationItem extends AbstractConfigurationItem {
	
	private boolean folder;
	
	public boolean isFolder() {
		return folder;
	}

	public PathConfigurationItem( String key, Configurable configurable,
			Field field, Class configuredClass, boolean folder ) {
		super( key, configurable, field, configuredClass );
		this.folder = folder;
	}

	@Override
	public Object getValueFromString(String value)
			throws ClassNotFoundException {
		return value != null ? Paths.get( value ) : null;
	}

	@Override
	public String toStringValue(Object value) {
		return ((Path)value).toAbsolutePath().toString();
	}

}
