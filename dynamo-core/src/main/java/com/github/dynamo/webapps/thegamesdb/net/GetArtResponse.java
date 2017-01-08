package com.github.dynamo.webapps.thegamesdb.net;

import org.simpleframework.xml.Element;

public class GetArtResponse {
	
	@Element
	private String baseImgUrl;
	
	@Element(name="Images")
	private TheGamesDBImageList images;

	public String getBaseImgUrl() {
		return baseImgUrl;
	}

	public void setBaseImgUrl(String baseImgUrl) {
		this.baseImgUrl = baseImgUrl;
	}

	public TheGamesDBImageList getImages() {
		return images;
	}

	public void setImages(TheGamesDBImageList images) {
		this.images = images;
	}

}
