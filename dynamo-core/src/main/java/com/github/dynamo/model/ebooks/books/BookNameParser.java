package com.github.dynamo.model.ebooks.books;

import com.github.dynamo.core.Language;
import com.github.mozvip.hclient.core.RegExp;

public class BookNameParser {
	
	public final static String[] nameCleaners = new String[] {
		"(.*)\\s+PDF",
		"(.*)\\s+PDF Français",
		"(.*)\\s+\\[fr\\]",
		"(.*)\\s+azw3",
		"(.*)\\s+mobi",
		"(.*)\\s+Epub",
		"(.*)\\s+\\[Epub\\]",
		"(.*)\\s+\\[Epub PDF\\]",
		"(.*)\\s+\\[PDF\\]",
		"(.*)\\s+\\+",
		"(.*)\\.EBOOK\\..*",
		"(.*)\\.FRENCH"
	};	

	public static BookInfo getBookInfo(String title) {
		
		title = title.trim();
		
		Language language = Language.FR;
		if (title.endsWith("[fr]")) {
			language = Language.FR;
		}
		
		title = RegExp.clean(title, nameCleaners);
		String[] groups = RegExp.parseGroups(title, "([\\wé]+,[\\wé]+) - ([\\w\\-]+)");
		if (groups != null) {
			return new BookInfo(groups[2], groups[1], language);
		}
		return new BookInfo(title, "Unknown", language);
	}

}
