package com.github.dynamo.webapps.thegamesdb.net.images;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="fanart")
public class TheGamesDBFanArt {
	
	@Attribute(name="width", required=false)
	private int width;
	
	@Attribute(name="height", required=false)
	private int height;
	
	@Element(name="original", required=false)
	private String original;
	
	@Element(name="thumb", required=false)
	private String thumb;

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getThumb() {
		return thumb;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}
	
	
	

}
