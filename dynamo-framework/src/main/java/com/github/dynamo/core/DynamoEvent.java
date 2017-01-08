package com.github.dynamo.core;

public class DynamoEvent {
	
	private String title;
	private DynamoEventType eventType;

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public DynamoEventType getEventType() {
		return eventType;
	}
	public void setEventType(DynamoEventType eventType) {
		this.eventType = eventType;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	private String body;

	public DynamoEvent(String title, DynamoEventType eventType, String body) {
		super();
		this.title = title;
		this.eventType = eventType;
		this.body = body;
	}

}
