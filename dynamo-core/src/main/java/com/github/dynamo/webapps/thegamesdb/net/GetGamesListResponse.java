package com.github.dynamo.webapps.thegamesdb.net;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="Data")
public class GetGamesListResponse {
	
	@ElementList(name="Game", inline=true, required=false)
	private List<TheGamesDBGame> games;
	
	@Element(name="baseImgUrl", required=false)
	private String baseImgUrl;
	
	public List<TheGamesDBGame> getGames() {
		return games;
	}
	
	public void setGames(List<TheGamesDBGame> games) {
		this.games = games;
	}
	
	public String getBaseImgUrl() {
		return baseImgUrl;
	}
	
	public void setBaseImgUrl(String baseImgUrl) {
		this.baseImgUrl = baseImgUrl;
	}

}
