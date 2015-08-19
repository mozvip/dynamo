package core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RegExp {
	
	private static LoadingCache<String, Pattern> patterns = CacheBuilder.newBuilder()
		       .build(
		           new CacheLoader<String, Pattern>() {
		        	   @Override
		        	   public Pattern load(String regex) {
		        		   return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		        	   }
		           });

	private RegExp() {
	}
	
	public static String clean( String text, String[] extractionRegexps ) {
		String cleanName = text.trim();
		boolean anotherPass = true;
		while (anotherPass) {
			for (String regExp : extractionRegexps) {
				String[] groups = RegExp.parseGroups(cleanName, regExp);
				if (groups != null) {
					cleanName = groups[0];
					anotherPass = true;
					continue;
				}
			}
			anotherPass = false;
		}

		return cleanName.trim();
	}
	
	public static String[] parseGroups( String text, String regex ) {
		
		if (text != null) {
		
			text = text.replace('\n', ' ');
			
			Pattern pattern = getPattern( regex );
			Matcher matcher = pattern.matcher( text );
			
			List<String> collection = new ArrayList<String>();
			if (matcher.matches()) {
				int count = matcher.groupCount();
				for (int i=1;i<=count; i++) {
					collection.add( matcher.group(i));
				}
			}
			
			if (!collection.isEmpty()) {
				return (String[]) collection.toArray(new String[collection.size()]);
			}
			
		}
		
		return null;
	}
	
	public static String keepOnlyGroups( String text, String regex) {
		String[] groups = RegExp.parseGroups( text,  regex );
		String result = text;
		if (groups != null) {
			result = StringUtils.join( groups, " ");
		}
		return result;
	}
	
	public static String extract( String text, String regex ) {
		String[] groups = parseGroups(text, regex);
		return groups != null ? groups[0] : null;
	}
	
	public static String filter( String text, String regex ) {
		String[] groups = parseGroups(text, regex);
		return groups != null ? groups[0] : text;
	}
	
	public static Pattern getPattern( String regex ) {
		try {
			return patterns.get(regex);
		} catch (ExecutionException e) {
			// bad boy
		}
		return null;
	}

	public static boolean matches(String word, String regex) {
		return matches( word, getPattern( regex ) );
	}
	
	public static boolean matches( String word, Pattern pattern ) {
		Matcher matcher = pattern.matcher( word );
		return matcher.matches();
	}	

}
