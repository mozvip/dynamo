package com.github.dynamo.webapps.thegamesdb.net;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public class GetPlatformsListResponse {
	
	@Element(name="basePlatformUrl")
	private String basePlatformUrl;
	
	@ElementList(name="Platforms")
	private List<TheGamesDBPlatform> platforms;

	public String getBasePlatformUrl() {
		return basePlatformUrl;
	}
	public void setBasePlatformUrl(String basePlatformUrl) {
		this.basePlatformUrl = basePlatformUrl;
	}
	public List<TheGamesDBPlatform> getPlatforms() {
		return platforms;
	}
	public void setPlatforms(List<TheGamesDBPlatform> platforms) {
		this.platforms = platforms;
	}
	
	

}
