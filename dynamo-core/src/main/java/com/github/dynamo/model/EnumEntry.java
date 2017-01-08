package com.github.dynamo.model;

public class EnumEntry {

	private String key;
	private String value;

	public EnumEntry(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
