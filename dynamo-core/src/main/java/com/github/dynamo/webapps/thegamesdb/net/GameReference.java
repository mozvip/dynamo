package com.github.dynamo.webapps.thegamesdb.net;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "Game")
public class GameReference {

	@Element
	private long id;

	@Element(name="PlatformId")
	private long platformId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPlatformId() {
		return platformId;
	}

	public void setPlatformId(long platformId) {
		this.platformId = platformId;
	}

}
