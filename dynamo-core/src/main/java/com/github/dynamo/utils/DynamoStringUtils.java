package com.github.dynamo.utils;

import java.text.Normalizer;

public class DynamoStringUtils {
	
	public static String removeAccents( String s ) {
		String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
		return temp.replaceAll("[^\\p{ASCII}]", "");
	}

}
