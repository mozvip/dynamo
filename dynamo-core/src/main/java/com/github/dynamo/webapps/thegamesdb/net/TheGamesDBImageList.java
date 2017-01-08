package com.github.dynamo.webapps.thegamesdb.net;

import java.util.List;

import org.simpleframework.xml.ElementList;

import com.github.dynamo.webapps.thegamesdb.net.images.TheGamesDBBanner;
import com.github.dynamo.webapps.thegamesdb.net.images.TheGamesDBBoxArt;
import com.github.dynamo.webapps.thegamesdb.net.images.TheGamesDBClearLogo;
import com.github.dynamo.webapps.thegamesdb.net.images.TheGamesDBFanArt;
import com.github.dynamo.webapps.thegamesdb.net.images.TheGamesDBScreenShot;

public class TheGamesDBImageList {

	@ElementList(inline=true, required=false)
	private List<TheGamesDBFanArt> fanarts;
	
	@ElementList(inline=true, required=false)
	private List<TheGamesDBBoxArt> boxarts;

	@ElementList(inline=true, required=false)
	private List<TheGamesDBBanner> banners;

	@ElementList(inline=true, required=false)
	private List<TheGamesDBScreenShot> screenshots;

	@ElementList(inline=true, required=false)
	private List<TheGamesDBClearLogo> clearlogos;

	public List<TheGamesDBFanArt> getFanarts() {
		return fanarts;
	}

	public void setFanarts(List<TheGamesDBFanArt> fanarts) {
		this.fanarts = fanarts;
	}

	public List<TheGamesDBBoxArt> getBoxarts() {
		return boxarts;
	}

	public void setBoxarts(List<TheGamesDBBoxArt> boxarts) {
		this.boxarts = boxarts;
	}

	public List<TheGamesDBBanner> getBanners() {
		return banners;
	}

	public void setBanners(List<TheGamesDBBanner> banners) {
		this.banners = banners;
	}

	public List<TheGamesDBScreenShot> getScreenshots() {
		return screenshots;
	}

	public void setScreenshots(List<TheGamesDBScreenShot> screenshots) {
		this.screenshots = screenshots;
	}

	public List<TheGamesDBClearLogo> getClearlogos() {
		return clearlogos;
	}

	public void setClearlogos(List<TheGamesDBClearLogo> clearlogos) {
		this.clearlogos = clearlogos;
	}
	
	
}
