package com.github.dynamo.core.logging;

public enum LogItemSeverity {
	
	DEBUG("Debug"), INFO("Info"), WARNING("Warning"), ERROR("Error");
	
	private String label;
	
	private LogItemSeverity( String label ) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}

}
