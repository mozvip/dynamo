package dynamo.core.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dynamo.core.manager.ErrorManager;
import liquibase.util.StringUtils;

public class MapperUtils {
	
	public static List<String> getStringList( String value ) {
		List<String> collection = null;
		if (value != null) {
			collection = StringUtils.splitAndTrim( value, ";" );
		}
		return collection;
	}

	public static List<? extends Enum> getEnumList( String value, Class<? extends Enum> enumClass ) {
		List<String> collection = getStringList(value);
		List<Enum> returnValue = null;
		if (collection != null) {
			returnValue = new ArrayList<>( collection.size() );
			for (String val : collection) {
				if (!org.apache.commons.lang3.StringUtils.isBlank(val)) {
					returnValue.add( Enum.valueOf(enumClass, val));
				}
			}
		}
		return returnValue;
	}

	public static Set<? extends Enum> getEnumSet( String value, Class<? extends Enum> enumClass ) {
		List<String> collection = getStringList(value);
		Set<Enum> returnValue = null;
		if (collection != null) {
			returnValue = new HashSet<>( collection.size() );
			for (String val : collection) {
				if (!org.apache.commons.lang3.StringUtils.isBlank(val)) {
					try {
						returnValue.add( Enum.valueOf(enumClass, val));
					} catch (Exception e) {
						ErrorManager.getInstance().logThrowable( e );
					}
				}
			}
		}
		return returnValue;
	}

	public static Path getPath(ResultSet r, String columnName) throws SQLException {
		String val = r.getString(columnName);
		return val != null ? Paths.get(val) : null;
	}

	public static <T extends Enum<T>> T getEnum(ResultSet r, String columnName, Class<T> enumType) throws SQLException {
		String name = r.getString(columnName);
		T val = null;
		if (name != null) {
			try {
				val = Enum.valueOf(enumType, name);
			} catch (java.lang.IllegalArgumentException e) {
				val = null;
			}
		}
		return val;
	}

}
