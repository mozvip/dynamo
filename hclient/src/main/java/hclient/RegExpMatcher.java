package hclient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpMatcher {

	public static boolean matches(String value, String patternStr) {
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}

	public static List<String> groups(String value, String patternStr) {
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(value);
		
		List<String> result = null;
		if (matcher.matches()) {
			result = new ArrayList<String>();
			for (int i=1; i<=matcher.groupCount(); i++) {
				result.add( matcher.group(i));
			}
		}
		return result;
	}

}
