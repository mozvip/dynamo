package com.github.dynamo.webapps.thegamesdb.net.images;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="clearlogo")
public class TheGamesDBClearLogo {
	
	@Attribute
	private int width;
	
	@Attribute
	private int height;

}
