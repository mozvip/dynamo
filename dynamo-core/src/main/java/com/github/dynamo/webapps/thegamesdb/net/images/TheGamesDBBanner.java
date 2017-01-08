package com.github.dynamo.webapps.thegamesdb.net.images;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name="banner")
public class TheGamesDBBanner {
	
	@Attribute(name="width", required=false)
	private int width;
	
	@Attribute(name="height", required=false)
	private int height;
	
	@Text
	private String path;
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public String getPath() {
		return path;
	}

}
