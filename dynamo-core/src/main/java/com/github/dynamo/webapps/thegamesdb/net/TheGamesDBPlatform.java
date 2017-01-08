package com.github.dynamo.webapps.thegamesdb.net;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="Platform")
public class TheGamesDBPlatform {
	
	@Element(name="id")
	private int id;
	@Element(name="name")
	private String name;
	@Element(name="alias", required=false)
	private String alias;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}

}
